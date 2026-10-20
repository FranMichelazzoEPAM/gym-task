package gym.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Base64;

public class CredentialsArgumentResolver  implements HandlerMethodArgumentResolver {
    private static final String BASIC_PREFIX = "Basic";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Credentials.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            throw new MissingCredentialsException("Missing or invalid Authorization header");
        }

        String base64Credentials = authHeader.substring(BASIC_PREFIX.length()).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64Credentials));
        } catch (IllegalArgumentException e) {
            throw new MissingCredentialsException("Malformed Authorization header");
        }

        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            throw new MissingCredentialsException("Malformed Authorization header");
        }

        return new Credentials(parts[0], parts[1]);
    }
}
