package binary.wz.diner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author binarywz
 * @date 2021/8/21 22:30
 * @description:
 */
@RestController
@RequestMapping("hello")
public class HelloController {

    @GetMapping
    public String hello(String name) {
        return "hello " + name;
    }

}
