package antifraud.auth;

import antifraud.handler.AntiFraudAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static antifraud.domain.UserRole.*;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final int ENCODER_STRENGTH = 13;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final AntiFraudAccessDeniedHandler accessDeniedHandler;


    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint, AntiFraudAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(getEncoder());
    }

    public void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .mvcMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(ROLE_ADMINISTRATOR.getDescription(), ROLE_SUPPORT.getDescription())
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(ROLE_MERCHANT.getDescription())
                .mvcMatchers("/api/antifraud/suspicious-ip", "/api/antifraud/suspicious-ip/**", "/api/antifraud/stolencard", "/api/antifraud/stolencard/**").hasRole(ROLE_SUPPORT.getDescription())
                .mvcMatchers(HttpMethod.PUT, "/api/auth/access", "/api/auth/role").hasRole(ROLE_ADMINISTRATOR.getDescription())
                .mvcMatchers(HttpMethod.DELETE, "/api/auth/**").hasRole(ROLE_ADMINISTRATOR.getDescription())
                .antMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .antMatchers("/actuator/shutdown").permitAll() // needs to run test
                // other matchers
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(ENCODER_STRENGTH);
    }
}
