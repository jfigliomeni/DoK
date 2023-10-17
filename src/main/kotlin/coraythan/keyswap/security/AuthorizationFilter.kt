package coraythan.keyswap.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException

class AuthorizationFilter(
        authMan: AuthenticationManager,
        private val jwtAuthService: JwtAuthService
) : BasicAuthenticationFilter(authMan) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
            req: HttpServletRequest,
            res: HttpServletResponse,
            chain: FilterChain
    ) {

        if (!jwtAuthService.hasJwtAuthHeader(req)) {
            chain.doFilter(req, res)
            return
        }

        SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthenticationFromJwt(req, res)
        chain.doFilter(req, res)
    }
}
