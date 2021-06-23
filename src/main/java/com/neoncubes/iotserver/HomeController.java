package com.neoncubes.iotserver;

import org.springframework.stereotype.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {

    // @Autowired
    // private UserController userController;

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    // FIXME: This is deprecated if using external frontend
    // @GetMapping(value = {"/signup", "/signin"})
    // public String index(Authentication authentication) {
    // logger.info("Loading page from index.html");
    // logger.info("Getting auth: {}", authentication);
    // if (authentication != null) {
    // logger.info("User is already logged in!");
    // } else {
    // logger.info("This user is currently anonymous");
    // }
    // return "index.html";
    // }

    // @RequestMapping("/api/account")
    // public void redirect(HttpServletRequest request, HttpServletResponse
    // response) throws IOException {
    //
    // logger.info("Getting request to be redirected: {}", request);
    // response.sendRedirect("/api/user");
    // }
}
