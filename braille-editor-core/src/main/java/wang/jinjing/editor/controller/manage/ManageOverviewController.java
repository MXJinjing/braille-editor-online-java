package wang.jinjing.editor.controller.manage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.jinjing.editor.pojo.VO.ManageOverviewVO;
import wang.jinjing.editor.service.ManageOverviewService;

@RestController
@RequestMapping("/api/manage/overview")
public class ManageOverviewController {

    @Autowired
    private ManageOverviewService manageOverviewService;

    @GetMapping("")
    public ManageOverviewVO getOverview(){
        return manageOverviewService.getOverview();
    }
}
