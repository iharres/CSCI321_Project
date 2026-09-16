package com.legoproject.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.ArrayList;

@CrossOrigin
@RestController
public class LegoController {

    @GetMapping("/api/sets")
    public List<LegoSet> getSets(){
        List<LegoSet> sets = new ArrayList<>();

        sets.add(new LegoSet(
                        "10294",
                        "Titanic",
                        500,
                        650
                        )
                        );

        return sets;
    }
}


