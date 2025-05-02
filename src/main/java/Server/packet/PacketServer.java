package Server.packet;

public class PacketServer {
    //  private static final Logger log = LoggerFactory.getLogger(PacketServer.class);
    //  private static ServerConnection init;
    //  private static boolean finishedShutdown = false;
    //  private static short port;
    //  private static ScheduledExecutorService scheduler;
    //  private static long intervalSeconds = ServerConfig.PACKET_SEND_INTERVAL_SECONDS; // 默认间隔秒数
//
    //  // 静态代码块，在类加载时执行
    //  static {
    //      port = ServerConfig.SEND_PACKET_PORT;
    //      try {
    //          init = new ServerConnection(port, -1, -1, ServerType.PacketServer);
    //          log.info("發包伺服器綁定連接埠: " + port + ".");
    //          // 创建线程池，用于定时发送数据包
    //          scheduler = Executors.newSingleThreadScheduledExecutor();
    //          // 定时任务：每隔 intervalSeconds 秒执行一次发送数据包的任务
    //          scheduler.scheduleAtFixedRate(PacketServer::sendDataToClients, 0, intervalSeconds, TimeUnit.SECONDS);
    //      } catch (Exception e) {
    //          try {
    //              throw new InvalidApplicationException("定時發包伺服器綁定連接埠 {port} 失敗");
    //          } catch (InvalidApplicationException ex) {
    //              throw new RuntimeException(ex);
    //          }
    //      }
    //  }
//
    //  // 发送数据包到所有已连接客户端
    //  private static void sendDataToClients() {
    //      // 遍历所有连接的客户端，发送数据包
    //      for (ServerConnection clientConnection : init.getConnectedClients()) {
    //          // 1. Choose a packet type (you'll need to define these elsewhere)
    //          PacketType packetType = choosePacketType(); // Replace this with your logic
//
    //          // 2. Build the packet based on the chosen type
    //          ByteBuf packet = buildPacket(packetType);
//
    //          // 3. Send the packet
    //          clientConnection.s(packet);
    //      }
    //  }
//
    //  // Placeholder for packet type selection (replace with your logic)
    //  private static PacketType choosePacketType() {
    //      // Example: randomly choose a packet type
    //      int randomType = (int) (Math.random() * 3);
    //      switch (randomType) {
    //          case 0: return PacketType.TYPE_1;
    //          case 1: return PacketType.TYPE_2;
    //          case 2: return PacketType.TYPE_3;
    //          default: return PacketType.TYPE_1;
    //      }
    //  }
//
    //  // Build a packet based on the given type
    //  private static ByteBuf buildPacket(PacketType type) {
    //      ByteBuf packet = Unpooled.buffer();
    //      switch (type) {
    //          case TYPE_1:
    //              packet.writeInt(12345);
    //              packet.writes("Hello, world!".getBytes());
    //              break;
    //          case TYPE_2:
    //              packet.writeShort(6789);
    //              packet.writeBoolean(true);
    //              break;
    //          case TYPE_3:
    //              // Add your logic for building TYPE_3 packet here
    //              break;
    //          default:
    //              // Handle unknown or invalid packet type
    //              break;
    //      }
    //      return packet;
    //  }
//
    //  public static int GetPort() {
    //      return port;
    //  }
//
    //  public static boolean Shutdown() {
    //      if (IsShutdown()) {
    //          return false;
    //      }
//
    //      log.info("正在關閉定時發包伺服器...");
    //      init.close();
    //      scheduler.shutdownNow();
    //      finishedShutdown = true;
    //      return true;
    //  }
//
    //  public static boolean IsShutdown() {
    //      return init == null || finishedShutdown;
    //  }
//
    //  public static void setIntervalSeconds(long intervalSeconds) {
    //      PacketServer.intervalSeconds = intervalSeconds;
    //      scheduler.scheduleAtFixedRate(PacketServer::sendDataToClients, 0, intervalSeconds, TimeUnit.SECONDS);
    //  }
//
    //  // Enum to represent different packet types (replace with your specific types)
    //  public enum PacketType {
    //      TYPE_1, TYPE_2, TYPE_3 // ... add more packet types as needed
    //  }
}