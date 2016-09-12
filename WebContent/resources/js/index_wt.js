//动态创建二级菜单树，传入的参数为一个数组对象
function menuTreeLoad(menuTree){
    var menuTreeLen=menuTree.menuList.length;
    //声明下面需要创建的节点，以便添加内容和添加到文档中
    var mainLi,firstA,firstI,labelSpan,iconSpan,secondUl,secondLi,secondA,publishSpan;
    //外层循环加载一级菜单内容
    for(var i=0;i<menuTreeLen;i++){
        mainLi=$("<li></li>");
        var menuGroupName=menuTree.menuList[i].menuGroupName;
        firstA=$("<a href='javascript:;'></a>");
        firstI=$("<i></i>");
        //通过一级菜单名称，添加对应图标
        switch(menuGroupName){
            case "节目管理":
                firstI.addClass("fa fa-home");
                break;
            case "发布管理":
                firstI.addClass("fa fa-table");
                break;
            default:
        }
        labelSpan=$("<span class='nav-label'></span>");
        labelSpan.text(menuGroupName);

        iconSpan=("<span class='fa arrow'></span>");

        firstA.append(firstI);
        firstA.append(labelSpan);
        firstA.append(iconSpan);
        mainLi.append(firstA);

        secondUl=$("<ul class='nav nav-second-level collapse'></ul>");
        var menuItemLen=menuTree.menuList[i].itemList.length;
        //内层菜单加载一级菜单对应的二级菜单列表
        for(var j=0;j<menuItemLen;j++){
            var itemName=menuTree.menuList[i].itemList[j].menuListName;
            var itemUrl=menuTree.menuList[i].itemList[j].menuListUrl;

            secondLi=$("<li></li");
            secondA=$("<a class='J_menuItem' href='#'></a>");
            secondA.attr({"href":itemUrl});
            /*
            if(menuGroupName=="发布管理"){
                publishSpan=$("<span class='listDot'></span>");
                //alert(j+itemName);
                switch(itemName){
                    case '待审核':
                        publishSpan.css({"border-color":"#da9b2a","background":"#febd2d"});
                        break;
                    case '已审核':
                        publishSpan.css({"border-color":"#00a643","background":"#00c940"});
                        break;
                    case '未通过':
                        publishSpan.css({"border-color":"#a32634","background":"#fe6255"});
                        break;
                    case '已撤回':
                        publishSpan.css({"border-color":"#7c7d7f","background":"#959595"});
                    break;
                    default:
                }
                secondA.html(publishSpan+itemName);
            }else{
                secondA.text(itemName);
            }*/
            secondA.text(itemName);
                secondUl.append(secondLi.append(secondA));
            }
        mainLi.append(secondUl);
        //将创建好的节点添加到对应位置
        $("#side-menu").prepend(mainLi);
    //整个for循环结束
    }
}