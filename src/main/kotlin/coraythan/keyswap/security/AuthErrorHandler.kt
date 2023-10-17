package coraythan.keyswap.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException

class AuthErrorHandler : AuthenticationEntryPoint {

    private val log = LoggerFactory.getLogger(AuthErrorHandler::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        log.debug("There was an authError ${authException.message}")
        if (response.status != 480) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
        }
    }

}
