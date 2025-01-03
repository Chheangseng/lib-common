package tcs.system.lib_common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class FilterSecurityInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler)
      throws Exception {
    if (handler instanceof HandlerMethod handlerMethod) {
      var method = handlerMethod.getMethod();
      if (method.isAnnotationPresent(AccessControl.class)) {
        var annotation = method.getAnnotation(AccessControl.class);
        System.out.println("---------------------------------");
        System.out.println(String.join(" ",annotation.role()));
      }
    }
    return HandlerInterceptor.super.preHandle(request, response, handler);
  }
}