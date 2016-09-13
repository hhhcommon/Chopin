package com.woting.favorite.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FavoriteController {

    /**
     * 点赞或举报
     * @param request
     * @return
     */
    @RequestMapping(value="clickFavorite.do")
    @ResponseBody
    public Map<String,Object> clickFavorite(HttpServletRequest request) {
        return null;
    }
}