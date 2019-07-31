package com.orange.malimacollector.controller;

import com.orange.malimacollector.entities.ConfluenceEntities.Page;
import com.orange.malimacollector.service.Confluence.ConfluenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ConfluenceController {
    @Autowired
    ConfluenceService confluenceService;

    @RequestMapping(method = RequestMethod.GET, value = "/confluence")
    public String confluenceDisplay(Model model){
        Page collection = (Page) confluenceService.handler(1);
        model.addAttribute("collection",collection);
        return "confluence";
    }
}