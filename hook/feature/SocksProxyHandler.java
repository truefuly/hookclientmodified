package hook.feature;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

/**
 * Minimal SOCKS4 / SOCKS5 client for the game connection (Minecraft does not ship Netty's proxy module).
 * Sits first in the pipeline: the TCP connect goes to the proxy, the handshake asks it to connect to the server,
 * and only then the connection counts as open for Minecraft (connect promise, channelActive, buffered writes).
 */
final class SocksProxyHandler extends ChannelDuplexHandler {
    private final ProxyManager.Proxy proxy;
    private InetSocketAddress target;
    private ChannelPromise userPromise;
    private final ArrayDeque<Object[]> pending = new ArrayDeque<>();
    private ByteBuf in;
    private int state; // socks5: 0 greeting, 1 auth, 2 connect; socks4: 10 connect
    private boolean done;

    SocksProxyHandler(ProxyManager.Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remote, SocketAddress local, ChannelPromise promise) {
        this.target = (InetSocketAddress) remote;
        this.userPromise = promise;
        ChannelPromise tcp = ctx.newPromise();
        tcp.addListener(f -> { if (!f.isSuccess()) this.fail(ctx, "can't reach the proxy " + this.proxy.host + ":" + this.proxy.port + " (" + f.cause().getMessage() + ")"); });
        ctx.connect(new InetSocketAddress(this.proxy.host, this.proxy.port), local, tcp);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.in = ctx.alloc().buffer();
        ctx.executor().schedule(() -> { if (!this.done) this.fail(ctx, "the proxy did not answer"); }, 10, java.util.concurrent.TimeUnit.SECONDS);
        if (this.proxy.type == ProxyManager.Type.SOCKS4) {
            this.state = 10;
            ByteBuf b = Unpooled.buffer();
            b.writeByte(4).writeByte(1).writeShort(this.target.getPort());
            byte[] ip = this.target.getAddress() == null ? new byte[]{0, 0, 0, 1} : this.target.getAddress().getAddress();
            boolean domain = this.target.getAddress() == null || ip.length != 4;
            b.writeBytes(domain ? new byte[]{0, 0, 0, 1} : ip); // SOCKS4a for names / IPv6
            b.writeBytes(this.proxy.user.getBytes(StandardCharsets.UTF_8)).writeByte(0);
            if (domain) b.writeBytes(this.target.getHostString().getBytes(StandardCharsets.US_ASCII)).writeByte(0);
            ctx.writeAndFlush(b);
        } else {
            this.state = 0;
            boolean auth = !this.proxy.user.isEmpty();
            ctx.writeAndFlush(Unpooled.wrappedBuffer(auth ? new byte[]{5, 2, 0, 2} : new byte[]{5, 1, 0}));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (this.done) { ctx.fireChannelRead(msg); return; }
        ByteBuf b = (ByteBuf) msg;
        this.in.writeBytes(b);
        b.release();
        try {
            this.step(ctx);
        } catch (Exception e) {
            this.fail(ctx, e.getMessage());
        }
    }

    private void step(ChannelHandlerContext ctx) {
        ByteBuf b = this.in;
        while (!this.done) {
            switch (this.state) {
                case 0 -> {
                    if (b.readableBytes() < 2) return;
                    if (b.readByte() != 5) throw new IllegalStateException("not a SOCKS5 proxy");
                    int method = b.readUnsignedByte();
                    if (method == 0) this.sendConnect(ctx);
                    else if (method == 2) {
                        byte[] u = this.proxy.user.getBytes(StandardCharsets.UTF_8), p = this.proxy.password.getBytes(StandardCharsets.UTF_8);
                        ByteBuf a = Unpooled.buffer();
                        a.writeByte(1).writeByte(u.length).writeBytes(u).writeByte(p.length).writeBytes(p);
                        ctx.writeAndFlush(a);
                        this.state = 1;
                    } else throw new IllegalStateException(this.proxy.user.isEmpty() ? "the proxy wants a username and password" : "the proxy refused the login method");
                }
                case 1 -> {
                    if (b.readableBytes() < 2) return;
                    b.readByte();
                    if (b.readByte() != 0) throw new IllegalStateException("wrong proxy username or password");
                    this.sendConnect(ctx);
                }
                case 2 -> {
                    if (b.readableBytes() < 5) return;
                    int type = b.getUnsignedByte(b.readerIndex() + 3);
                    int len = 4 + (type == 1 ? 4 : type == 4 ? 16 : 1 + b.getUnsignedByte(b.readerIndex() + 4)) + 2;
                    if (b.readableBytes() < len) return;
                    int rep = b.getUnsignedByte(b.readerIndex() + 1);
                    b.skipBytes(len);
                    if (rep != 0) throw new IllegalStateException("the proxy could not reach the server (" + socks5Error(rep) + ")");
                    this.success(ctx);
                }
                case 10 -> {
                    if (b.readableBytes() < 8) return;
                    b.readByte();
                    int rep = b.readUnsignedByte();
                    b.skipBytes(6);
                    if (rep != 0x5A) throw new IllegalStateException("the proxy refused the connection (code " + rep + ")");
                    this.success(ctx);
                }
                default -> { return; }
            }
        }
    }

    private void sendConnect(ChannelHandlerContext ctx) {
        ByteBuf c = Unpooled.buffer();
        c.writeByte(5).writeByte(1).writeByte(0);
        if (this.target.getAddress() == null) {
            byte[] host = this.target.getHostString().getBytes(StandardCharsets.US_ASCII);
            c.writeByte(3).writeByte(host.length).writeBytes(host);
        } else {
            byte[] ip = this.target.getAddress().getAddress();
            c.writeByte(ip.length == 4 ? 1 : 4).writeBytes(ip);
        }
        c.writeShort(this.target.getPort());
        ctx.writeAndFlush(c);
        this.state = 2;
    }

    private void success(ChannelHandlerContext ctx) {
        this.done = true;
        ByteBuf rest = this.in.readableBytes() > 0 ? this.in.retainedSlice() : null;
        this.in.release();
        this.userPromise.trySuccess();
        ctx.fireChannelActive();
        while (!this.pending.isEmpty()) {
            Object[] w = this.pending.poll();
            ctx.write(w[0], (ChannelPromise) w[1]);
        }
        ctx.flush();
        if (rest != null) ctx.fireChannelRead(rest);
        ctx.pipeline().remove(this); // from here on the connection is plain Minecraft traffic
    }

    private void fail(ChannelHandlerContext ctx, String why) {
        if (this.done) return;
        this.done = true;
        if (this.in != null && this.in.refCnt() > 0) this.in.release();
        Exception e = new java.net.ConnectException("Proxy: " + why);
        while (!this.pending.isEmpty()) {
            Object[] w = this.pending.poll();
            io.netty.util.ReferenceCountUtil.release(w[0]);
            ((ChannelPromise) w[1]).tryFailure(e);
        }
        if (this.userPromise != null) this.userPromise.tryFailure(e);
        ctx.close();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (this.done) ctx.write(msg, promise);
        else this.pending.add(new Object[]{msg, promise});
    }

    @Override
    public void flush(ChannelHandlerContext ctx) {
        if (this.done) ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!this.done) this.fail(ctx, String.valueOf(cause.getMessage()));
        else ctx.fireExceptionCaught(cause);
    }

    private static String socks5Error(int rep) {
        return switch (rep) {
            case 1 -> "general failure";
            case 2 -> "not allowed by the proxy";
            case 3 -> "network unreachable";
            case 4 -> "host unreachable";
            case 5 -> "connection refused";
            case 6 -> "TTL expired";
            default -> "code " + rep;
        };
    }
}
