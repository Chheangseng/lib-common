package tcs.system.lib_common.ipUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class IPAddressUtils {

  public static String getMachineIPAddress() {
    try {
      return NetworkInterface.networkInterfaces()
          .filter(
              (var networkInterface) -> {
                try {
                  return (networkInterface.isUp()
                      && !networkInterface.isVirtual()
                      && !networkInterface.isLoopback());
                } catch (SocketException e) {
                  if (log.isDebugEnabled()) {
                    log.debug("Error while check network interface", e);
                  }
                  return false;
                }
              })
          .flatMap(NetworkInterface::inetAddresses)
          .filter(Inet4Address.class::isInstance)
          .findFirst()
          .orElse(InetAddress.getLocalHost())
          .getHostAddress();
    } catch (SocketException | UnknownHostException e) {
      log.error("Error while try to detect server IPv4", e);
      return "127.0.0.1";
    }
  }


  public static String getIpAddressOrServletRequestIp(String ipAddress) {
    if (Objects.nonNull(ipAddress)) {
      return ipAddress;
    }

    var requestAttributes = RequestContextHolder.getRequestAttributes();
    if (Objects.nonNull(requestAttributes)) {
      String remoteAddr =
          ((ServletRequestAttributes) requestAttributes).getRequest().getRemoteAddr();

      if (Objects.equals(remoteAddr, "0:0:0:0:0:0:0:1")
          || Objects.equals(remoteAddr, "127.0.0.1")) {
        remoteAddr = getMachineIPAddress();
      }

      return remoteAddr;
    }

    return ipAddress;
  }

  public static String getIpAddressFromUrl(String url) {
    try {
      return InetAddress.getByName(new URL(url).getHost()).getHostAddress();
    } catch (MalformedURLException | UnknownHostException e) {
      log.error("Can not get ip from url", e);
    }
    return null;
  }
}
