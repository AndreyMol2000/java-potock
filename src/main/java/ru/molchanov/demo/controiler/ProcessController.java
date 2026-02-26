package ru.molchanov.demo.controiler;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.molchanov.demo.servise.ProcessSerice;

@RestController
public class ProcessController {
    private  final ProcessSerice processSerice;

    public ProcessController(ProcessSerice processSerice) {
        this.processSerice = processSerice;
    }

    @GetMapping("/hello")
    public String hello() {

        return "Hello";
    }

    @GetMapping("/test/process")
    public String process() {

        return processSerice.process();
    }
}
