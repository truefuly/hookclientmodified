package hook.feature;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * The math of the seed cracker (no Minecraft classes in here).
 *
 * <ul>
 *   <li>Every structure of a "random spread" set (temples, villages, outposts, monuments...) sits at the one chunk of
 *   its region that the world seed picks: {@code setSeed(regionX*341873128712 + regionZ*132897987541 + seed + salt)}
 *   followed by {@code nextInt(spacing - separation)} twice. Only the lower 48 bits of the seed take part.</li>
 *   <li>For an even {@code spacing - separation} the lowest bits of that {@code nextInt} only depend on the lowest
 *   20 bits of the seed, so those are brute forced first (2^20), filtered by every exact structure.</li>
 *   <li>The End pillars come from {@code new Random(seed).nextLong() & 0xFFFF}: that pins bits 16..31 of the second
 *   random state, i.e. a filter on the lowest 32 bits of the seed.</li>
 *   <li>The rest of the 48 bits is brute forced on all cores against every structure (full check).</li>
 *   <li>The top 16 bits come from the hashed seed every server sends at join (first 8 bytes of SHA-256 of the seed,
 *   used for biome zoom): 65536 tries per 48-bit candidate.</li>
 * </ul>
 */
public final class SeedCracker {
    private SeedCracker() {
    }

    static final long MULT = 0x5DEECE66DL, ADD = 0xBL, MASK = (1L << 48) - 1;
    static final long K1 = 341873128712L, K2 = 132897987541L;

    /** A vanilla structure set (1.21.11 data/minecraft/worldgen/structure_set). reduce: 0 none, 1 outpost 1/5, 2 buried treasure 1%. */
    public record Type(String id, String name, int spacing, int separation, int salt, boolean triangular, int reduce, int tolerance) {
        int n() { return this.spacing - this.separation; }
    }

    public static final Map<String, Type> TYPES = new LinkedHashMap<>();

    private static void type(String id, String name, int spacing, int sep, int salt, boolean tri, int reduce, int tol) {
        TYPES.put(id, new Type(id, name, spacing, sep, salt, tri, reduce, tol));
    }

    static {
        // tolerance: how many chunks off a hand-entered position may be (jigsaw structures spread around their start chunk)
        type("desert_pyramid", "Desert Pyramid", 32, 8, 14357617, false, 0, 0);
        type("jungle_temple", "Jungle Temple", 32, 8, 14357619, false, 0, 0);
        type("swamp_hut", "Swamp Hut", 32, 8, 14357620, false, 0, 0);
        type("igloo", "Igloo", 32, 8, 14357618, false, 0, 0);
        type("village", "Village", 34, 8, 10387312, false, 0, 1);
        type("outpost", "Pillager Outpost", 32, 8, 165745296, false, 1, 1);
        type("shipwreck", "Shipwreck", 24, 4, 165745295, false, 0, 1);
        type("ocean_ruin", "Ocean Ruin", 20, 8, 14357621, false, 0, 0);
        type("monument", "Ocean Monument", 32, 5, 10387313, true, 0, 0);
        type("mansion", "Woodland Mansion", 80, 20, 10387319, true, 0, 0);
        type("ruined_portal", "Ruined Portal", 40, 15, 34222645, false, 0, 0);
        type("trail_ruins", "Trail Ruins", 34, 8, 83469867, false, 0, 1);
        type("trial_chambers", "Trial Chambers", 34, 12, 94251327, false, 0, 1);
        type("ancient_city", "Ancient City", 24, 8, 20083232, false, 0, 1);
        type("end_city", "End City", 20, 11, 10387313, true, 0, 0);
        type("nether_complex", "Fortress / Bastion", 27, 4, 30084232, false, 0, 1);
        type("buried_treasure", "Buried Treasure", 1, 0, 0, false, 2, 0);
    }

    /** One structure seen in the world: the chunk its generation started in. */
    public static final class Obs {
        public final Type type;
        public final int cx, cz, tol;
        public final boolean auto;
        final int rx, rz, ox, oz;

        public Obs(Type type, int cx, int cz, int tol, boolean auto) {
            this.type = type;
            this.cx = cx;
            this.cz = cz;
            this.tol = tol;
            this.auto = auto;
            this.rx = Math.floorDiv(cx, type.spacing);
            this.rz = Math.floorDiv(cz, type.spacing);
            this.ox = cx - this.rx * type.spacing;
            this.oz = cz - this.rz * type.spacing;
        }

        /** Bits of the seed this one tells (estimate). */
        double bits() {
            int n = this.type.n();
            double b;
            if (this.type.reduce == 2) return Math.log(100) / Math.log(2);
            if (this.type.triangular) b = 2 * (Math.log(n) / Math.log(2)) - 1.0;
            else b = 2 * (Math.log(n) / Math.log(2));
            b -= 2 * (Math.log(2 * this.tol + 1) / Math.log(2));
            if (this.type.reduce == 1) b += Math.log(5) / Math.log(2);
            return Math.max(0, b);
        }

        /** How many of the lowest bits of each offset the low-20-bit stage can test (0 = none). */
        int lowBits() {
            int n = this.type.n();
            if (this.tol != 0 || this.type.triangular || this.type.reduce == 2 || n <= 1 || (n & (n - 1)) == 0) return 0;
            return Math.min(3, Integer.numberOfTrailingZeros(n));
        }

        boolean lowCheck(long low20, int k) {
            long m = 0xFFFFFL, km = (1L << k) - 1;
            long s = ((this.rx * K1 + this.rz * K2 + low20 + this.type.salt) ^ MULT) & m;
            s = (s * MULT + ADD) & m;
            if (((s >>> 17) & km) != (this.ox & km)) return false;
            s = (s * MULT + ADD) & m;
            return ((s >>> 17) & km) == (this.oz & km);
        }

        boolean check(long seed, Rng r) {
            Type t = this.type;
            if (t.spacing <= 1) return reduce(seed, this.cx, this.cz, r);
            if (this.tol == 0) return place(seed, this.rx, this.rz, r);
            int sp = t.spacing;
            for (int rx = Math.floorDiv(this.cx - this.tol, sp); rx <= Math.floorDiv(this.cx + this.tol, sp); rx++)
                for (int rz = Math.floorDiv(this.cz - this.tol, sp); rz <= Math.floorDiv(this.cz + this.tol, sp); rz++)
                    if (place(seed, rx, rz, r)) return true;
            return false;
        }

        /** Does the region (rx, rz) put its structure within tol of the seen chunk? */
        private boolean place(long seed, int rx, int rz, Rng r) {
            Type t = this.type;
            int sp = t.spacing, n = t.n();
            r.s = ((rx * K1 + rz * K2 + seed + t.salt) ^ MULT) & MASK;
            int px, pz;
            if (t.triangular) {
                px = (r.nextInt(n) + r.nextInt(n)) / 2;
                pz = (r.nextInt(n) + r.nextInt(n)) / 2;
            } else {
                px = r.nextInt(n);
                if (Math.abs(rx * sp + px - this.cx) > this.tol) return false;
                pz = r.nextInt(n);
            }
            int pcx = rx * sp + px, pcz = rz * sp + pz;
            if (Math.abs(pcx - this.cx) > this.tol || Math.abs(pcz - this.cz) > this.tol) return false;
            return reduce(seed, pcx, pcz, r);
        }

        private boolean reduce(long seed, int cx, int cz, Rng r) {
            Type t = this.type;
            if (t.reduce == 1) {
                int m = cx >> 4, n = cz >> 4;
                r.s = (((long) (m ^ (n << 4))) ^ seed ^ MULT) & MASK;
                r.next(32);
                return r.nextInt(5) == 0;
            }
            if (t.reduce == 2) {
                r.s = ((cx * K1 + cz * K2 + seed + 10387320L) ^ MULT) & MASK;
                return r.next(24) / (float) (1 << 24) < 0.01f;
            }
            return true;
        }

        @Override
        public String toString() {
            return this.type.id + " " + this.cx + " " + this.cz + " " + this.tol + " " + (this.auto ? "auto" : "manual");
        }
    }

    /** java.util.Random without the object. */
    static final class Rng {
        long s;

        int next(int bits) {
            this.s = (this.s * MULT + ADD) & MASK;
            return (int) (this.s >>> (48 - bits));
        }

        int nextInt(int n) {
            if ((n & -n) == n) return (int) ((long) n * this.next(31) >> 31);
            int bits, val;
            do {
                bits = this.next(31);
                val = bits % n;
            } while (bits - val + (n - 1) < 0);
            return val;
        }
    }

    // ------------------------------------------------------------------ End pillars

    /** Pillar index 0..9 → height (76..103), or -1 if unknown. Gives the pillar seeds (0..65535) that build them like this. */
    static int[] pillarSeeds(int[] heights) {
        int known = 0;
        for (int h : heights) if (h >= 0) known++;
        if (known < 3) return null;
        List<Integer> out = new ArrayList<>();
        Rng r = new Rng();
        int[] list = new int[10];
        for (int l = 0; l < 65536; l++) {
            r.s = (l ^ MULT) & MASK;
            for (int i = 0; i < 10; i++) list[i] = i;
            for (int j = 10; j > 1; j--) {
                int k = r.nextInt(j);
                int tmp = list[j - 1];
                list[j - 1] = list[k];
                list[k] = tmp;
            }
            boolean ok = true;
            for (int i = 0; i < 10 && ok; i++) if (heights[i] >= 0 && 76 + 3 * list[i] != heights[i]) ok = false;
            if (ok) out.add(l);
        }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    /** bits 16..31 of the second state of new Random(seed): the low 16 bits of nextLong(). Needs the lowest 32 bits of the seed. */
    static int pillarOf(long seed) {
        long s = (seed ^ MULT) & MASK;
        s = (s * MULT + ADD) & MASK;
        s = (s * MULT + ADD) & MASK;
        return (int) (s >>> 16) & 0xFFFF;
    }

    // ------------------------------------------------------------------ hashed seed

    /** BiomeManager.obfuscateSeed: SHA-256 of the seed (little endian), first 8 bytes as little endian long. */
    static long hashSeed(MessageDigest md, long seed) {
        byte[] in = new byte[8];
        for (int i = 0; i < 8; i++) in[i] = (byte) (seed >>> (8 * i));
        byte[] h = md.digest(in);
        long v = 0;
        for (int i = 7; i >= 0; i--) v = (v << 8) | (h[i] & 0xFFL);
        return v;
    }

    private static final int[] K = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01,
        0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147,
        0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08,
        0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
        0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

    /** Same as {@link #hashSeed} without MessageDigest (one 64-byte block, padding fixed): a few times faster. */
    static long hashFast(long seed, int[] w) {
        w[0] = Integer.reverseBytes((int) seed);
        w[1] = Integer.reverseBytes((int) (seed >>> 32));
        w[2] = 0x80000000;
        for (int i = 3; i < 15; i++) w[i] = 0;
        w[15] = 64;
        for (int i = 16; i < 64; i++) {
            int a = w[i - 15], b = w[i - 2];
            int s0 = Integer.rotateRight(a, 7) ^ Integer.rotateRight(a, 18) ^ (a >>> 3);
            int s1 = Integer.rotateRight(b, 17) ^ Integer.rotateRight(b, 19) ^ (b >>> 10);
            w[i] = w[i - 16] + s0 + w[i - 7] + s1;
        }
        int a = 0x6a09e667, b = 0xbb67ae85, c = 0x3c6ef372, d = 0xa54ff53a, e = 0x510e527f, f = 0x9b05688c, g = 0x1f83d9ab, h = 0x5be0cd19;
        for (int i = 0; i < 64; i++) {
            int t1 = h + (Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25)) + ((e & f) ^ (~e & g)) + K[i] + w[i];
            int t2 = (Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22)) + ((a & b) ^ (a & c) ^ (b & c));
            h = g;
            g = f;
            f = e;
            e = d + t1;
            d = c;
            c = b;
            b = a;
            a = t1 + t2;
        }
        int h0 = 0x6a09e667 + a, h1 = 0xbb67ae85 + b;
        return Long.reverseBytes(((long) h0 << 32) | (h1 & 0xFFFFFFFFL));
    }

    static MessageDigest sha() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------ the run

    public static final class Result {
        public volatile String stage = "idle";
        public volatile double progress;
        public volatile Long seed;
        public volatile long[] structureSeeds;
        public volatile String error;
        public volatile boolean done;
        public volatile Obs badObs;
    }

    static final long MAX_WORK = 1L << 34, MAX_STRUCT = 1 << 18;
    /** Self-test only: the real seed, to say which check throws it out. */
    static volatile Long debugTruth;
    /**
     * Structures of the same kind (and even different kinds: the salt is only added) are far from independent - the
     * region seeds differ by constants, so the first random numbers come out related. Measured: about 42% of the
     * naive bits are real (8 temples: ~32 of 73).
     */
    static final double REAL = 0.42;

    static double bits(List<Obs> obs, int[] pillars) {
        double bits = 0;
        for (Obs o : obs) bits += o.bits();
        bits *= REAL;
        if (pillars != null && pillars.length > 0) bits += 16 - Math.log(pillars.length) / Math.log(2);
        return bits;
    }

    /** Lowest-20-bit candidates (cheap: 2^20 small checks). */
    static long[] low20(List<Obs> obs) {
        List<Obs> lows = new ArrayList<>();
        for (Obs o : obs) if (o.lowBits() > 0) lows.add(o);
        long[] cand = new long[1024];
        int nc = 0;
        for (long l = 0; l < (1L << 20); l++) {
            boolean ok = true;
            for (Obs o : lows) if (!o.lowCheck(l, o.lowBits())) { ok = false; break; }
            if (!ok) continue;
            if (nc == cand.length) cand = Arrays.copyOf(cand, nc * 2);
            cand[nc++] = l;
        }
        return Arrays.copyOf(cand, nc);
    }

    static long work(List<Obs> obs, int[] pillars, int low20) {
        if (pillars != null && pillars.length > 0) return Math.max(1L, (long) low20 * 4096L * pillars.length / 65536L) * (1L << 16);
        return (long) low20 * (1L << 28);
    }

    /** Whether a run can finish: null = go, otherwise what is missing. */
    static String ready(List<Obs> obs, int[] pillars, long hashed) {
        double bits = bits(obs, pillars);
        double need = hashed != 0 ? 48 - 18 : 46;
        long work = work(obs, pillars, low20(obs).length);
        if (bits < need) return String.format("%.0f of ~%.0f bits", bits, need);
        if (work > MAX_WORK) return "need more temples / huts (too many to try)";
        return null;
    }

    /** The 48-bit structure seeds that fit everything; null if too much work (res.error set). */
    static long[] structureSeeds(List<Obs> obsIn, int[] pillars, int threads, Result res, AtomicBoolean cancel, Consumer<String> log) throws InterruptedException {
        List<Obs> obs = new ArrayList<>(obsIn);
        obs.sort(Comparator.comparingDouble(Obs::bits).reversed());
        res.stage = "low bits";
        long[] cand = low20(obs);
        int nc = cand.length;
        int bitsKnown = 20;
        log.accept("low 20 bits: " + nc + " left");
        boolean usePill = pillars != null && pillars.length > 0;
        final boolean[] okPill = new boolean[65536];
        if (usePill) {
            for (int p : pillars) okPill[p] = true;
            res.stage = "pillars";
            long[] c2 = new long[1024];
            int n2 = 0;
            for (int i = 0; i < nc; i++) {
                for (long h = 0; h < 4096; h++) {
                    long l32 = cand[i] | (h << 20);
                    if (!okPill[pillarOf(l32)]) continue;
                    if (n2 == c2.length) c2 = Arrays.copyOf(c2, n2 * 2);
                    c2[n2++] = l32;
                }
            }
            cand = c2;
            nc = n2;
            bitsKnown = 32;
            log.accept("low 32 bits (pillars): " + nc + " left");
        }
        long upper = 1L << (48 - bitsKnown);
        if ((long) nc * upper > MAX_WORK * 2) {
            res.error = "not enough structures yet (" + nc + " x 2^" + (48 - bitsKnown) + " to try)";
            return null;
        }
        res.stage = "structure seed";
        res.progress = 0;
        ConcurrentLinkedQueue<Long> found = new ConcurrentLinkedQueue<>();
        AtomicInteger foundN = new AtomicInteger();
        long total = Math.max(1, (long) nc * upper);
        AtomicLong doneWork = new AtomicLong();
        final long[] fc = cand;
        final int fb = bitsKnown;
        final Obs[] arr = obs.toArray(new Obs[0]);
        long slice = Math.max(1, Math.min(upper, 1L << 22));
        long perCand = (upper + slice - 1) / slice;
        long slices = (long) nc * perCand;
        AtomicLong next = new AtomicLong();
        Thread[] ts = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            ts[t] = new Thread(() -> {
                Rng r = new Rng();
                long job;
                while (!cancel.get() && (job = next.getAndIncrement()) < slices) {
                    long low = fc[(int) (job / perCand)];
                    long from = (job % perCand) * slice, to = Math.min(upper, from + slice);
                    for (long u = from; u < to; u++) {
                        long seed = low | (u << fb);
                        boolean ok = true;
                        for (Obs o : arr) if (!o.check(seed, r)) { ok = false; break; }
                        if (ok && usePill && !okPill[pillarOf(seed)]) ok = false;
                        if (ok && foundN.incrementAndGet() <= MAX_STRUCT) found.add(seed);
                    }
                    res.progress = doneWork.addAndGet(to - from) / (double) total;
                }
            }, "Hook SeedCracker " + t);
            ts[t].setDaemon(true);
            ts[t].setPriority(Thread.MIN_PRIORITY);
            ts[t].start();
        }
        for (Thread t : ts) t.join();
        if (cancel.get()) { res.error = "cancelled"; return null; }
        log.accept("structure seeds: " + foundN.get());
        Long truth = debugTruth;
        if (truth != null) {
            long tl = truth & MASK;
            Rng r = new Rng();
            StringBuilder sb = new StringBuilder("truth check:");
            for (Obs o : obs) sb.append(' ').append(o.type.id).append(o.check(tl, r) ? "+" : "-").append(o.lowBits() > 0 ? (o.lowCheck(tl & 0xFFFFF, o.lowBits()) ? "L+" : "L-") : "");
            if (usePill) sb.append(" pillar ").append(pillarOf(tl)).append(" in ").append(Arrays.toString(pillars));
            log.accept(sb.toString());
        }
        if (foundN.get() > MAX_STRUCT) {
            res.error = foundN.get() + " structure seeds fit - add a few more structures";
            return null;
        }
        return found.stream().mapToLong(Long::longValue).sorted().toArray();
    }

    /** Runs the whole crack on all cores. Blocking; call from a worker thread. */
    static void run(List<Obs> obsIn, int[] pillars, long hashed, int threads, Result res, AtomicBoolean cancel, Consumer<String> log) {
        try {
            long[] ss = structureSeeds(obsIn, pillars, threads, res, cancel, log);
            if (ss == null) return;
            if (ss.length == 0 && obsIn.size() >= 5) {
                // one of them is wrong (misread, a /place'd copy, a modded spot...): leave each out once
                res.stage = "finding the wrong structure";
                for (int i = obsIn.size() - 1; i >= 0 && !cancel.get(); i--) {
                    List<Obs> sub = new ArrayList<>(obsIn);
                    Obs bad = sub.remove(i);
                    if (work(sub, pillars, low20(sub).length) > MAX_WORK) continue;
                    SeedCracker.Result tmp = new SeedCracker.Result();
                    long[] s2 = structureSeeds(sub, pillars, threads, tmp, cancel, x -> { });
                    if (s2 != null && s2.length > 0) {
                        res.badObs = bad;
                        ss = s2;
                        log.accept("left out " + bad + ": " + s2.length + " fit");
                        break;
                    }
                }
            }
            res.structureSeeds = ss;
            if (ss.length == 0) {
                res.error = "no seed fits - a structure is probably wrong (or the server uses a modded world)";
                return;
            }
            if (hashed == 0) {
                res.error = ss.length == 1 ? "server sends no hashed seed - only the structure seed is known" : "server sends no hashed seed - " + ss.length + " structure seeds fit, add structures";
                return;
            }
            // top 16 bits from the hashed seed
            res.stage = "full seed";
            res.progress = 0;
            AtomicLong hit = new AtomicLong();
            AtomicBoolean any = new AtomicBoolean();
            AtomicInteger idx = new AtomicInteger();
            AtomicInteger doneS = new AtomicInteger();
            final long[] fss = ss;
            Thread[] ts = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                ts[t] = new Thread(() -> {
                    int[] w = new int[64];
                    int i;
                    while (!cancel.get() && !any.get() && (i = idx.getAndIncrement()) < fss.length) {
                        long base = fss[i];
                        for (long u = 0; u < 65536; u++) {
                            long seed = base | (u << 48);
                            if (hashFast(seed, w) == hashed) { hit.set(seed); any.set(true); break; }
                        }
                        res.progress = doneS.incrementAndGet() / (double) fss.length;
                    }
                }, "Hook SeedCracker hash " + t);
                ts[t].setDaemon(true);
                ts[t].setPriority(Thread.MIN_PRIORITY);
                ts[t].start();
            }
            for (Thread t : ts) t.join();
            if (any.get()) {
                res.seed = hit.get();
                res.structureSeeds = new long[]{hit.get() & MASK};
            } else if (!cancel.get()) {
                res.error = "the hashed seed matches none of the " + ss.length + " structure seeds (server may fake it)";
            } else {
                res.error = "cancelled";
            }
        } catch (Throwable t) {
            res.error = "crashed: " + t;
        } finally {
            res.done = true;
        }
    }
}
