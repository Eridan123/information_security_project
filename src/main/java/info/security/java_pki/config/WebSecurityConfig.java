package info.security.java_pki.config;


import info.security.java_pki.repository.UserRepository;
import info.security.java_pki.service.UserDetailsServiceImpl;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.sql.DataSource;
import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

//         Setting Service to find User in the database.
//         And Setting PassswordEncoder
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        // The pages does not require login
        http.authorizeRequests().antMatchers("/", "/login","/forgot","/reset/**","/logout","/user/0/save","/lawyer/top").permitAll();
        // /userInfo page requires login as ROLE_USER or ROLE_ADMIN.
        // If no login, it will redirect to /login page.
//        http.authorizeRequests().antMatchers("/user/**").access("hasAnyRole('ROLE_USER','ROLE_LAWYER', 'ROLE_ADMIN')");

        // For ADMIN only.
//        http.authorizeRequests().antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')");

        // When the user has logged in as XX.
        // But access a page that requires role YY,
        // AccessDeniedException will be thrown.
        http.authorizeRequests().and().exceptionHandling().accessDeniedPage("/403");
        http.authorizeRequests().anyRequest().permitAll();

        // Config for Login Form
        http.authorizeRequests().and().formLogin()//
                // Submit URL of login page.
                .loginProcessingUrl("/j_spring_security_check") // Submit URL
                .loginPage("/login")//
                .defaultSuccessUrl("/")//
                .failureUrl("/login?error=true")//
                .usernameParameter("username")//
                .passwordParameter("password")

                // Config for Logout Page
                .and().logout().logoutUrl("/logout").logoutSuccessUrl("/");

        // Config Remember Me.
        http.authorizeRequests().and() //
                .rememberMe().tokenRepository(this.persistentTokenRepository()) //
                .tokenValiditySeconds(1 * 24 * 60 * 60); // 24h

    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }

    @Override
    public void configure(WebSecurity web){
        web
                .ignoring()
                .antMatchers("/resources/**","/ajax/**","/assets/**","/vendor/**","/styles/**","/plugins/**","/fonts/**","/javascripts/**","/stylesheets/**","/static/**","/css/**","/js/**","/images/**");
    }


    @Value("${trust.store}")
    private File trustStore;
    @Value("${trust.store.password}")
    private String trustStorePassword;


    @Bean
    RestTemplate restTemplate() throws Exception {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(trustStore, trustStorePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Test
    public void whenGETanHTTPSResource_thenCorrectResponse() throws Exception {
        ResponseEntity<String> response =
                restTemplate().getForEntity("/login", String.class, Collections.emptyMap());

        assertEquals("<h1>Welcome to Secured Site</h1>", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
