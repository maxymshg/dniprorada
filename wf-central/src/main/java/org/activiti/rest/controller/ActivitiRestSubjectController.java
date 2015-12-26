package org.activiti.rest.controller;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wf.dp.dniprorada.dao.SubjectDao;
import org.wf.dp.dniprorada.dao.SubjectHumanDao;
import org.wf.dp.dniprorada.dao.SubjectOrganDao;
import org.wf.dp.dniprorada.model.Subject;
import org.wf.dp.dniprorada.model.SubjectHuman;
import org.wf.dp.dniprorada.model.SubjectOrgan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import javax.servlet.http.HttpServletResponse;

@Controller
@Api(tags = { "ActivitiRestSubjectController" }, description = "Работа с субъектами")
@RequestMapping(value = "/subject")
public class ActivitiRestSubjectController {

    private static final Logger LOG = LoggerFactory.getLogger(ActivitiRestSubjectController.class);

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Подробные описания сервисов для документирования в Swagger
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String noteCODE= "\n```\n";    
    private static final String noteCODEJSON= "\n```json\n";    
    private static final String noteController = "##### Работа с субъектами. ";

    private static final String noteSyncSubject = noteController + "Получение субъекта #####\n\n"
		+ "HTTP Context: http://server:port/wf/service/subject/syncSubject\n\n\n"
		+ "Если субъект найден, или добавление субъекта в противном случае\n\n"

		+ "От клиента ожидается ОДИН и только ОДИН параметр из нижеперечисленных\n\n"

		+ "- nID - ИД-номер субъекта\n"
		+ "- sINN - строка-ИНН (субъект - человек)\n"
		+ "- sOKPO - строка-ОКПО (субъек - организация)\n"
		+ "- nID_Subject - ID авторизированого субъекта (добавляется в запрос автоматически после аутентификации пользователя)\n\n\n"
		+ "Примеры:\n\n"
		+ "https://test.igov.org.ua/wf/service/subject/syncSubject?sINN=34125265377\n\n"
		+ "https://test.igov.org.ua/wf/service/subject/syncSubject?sOKPO=123\n\n"
		+ "https://test.igov.org.ua/wf/service/subject/syncSubject?nID=1\n\n"
		+ "Response\n"
		+ noteCODEJSON
		+ "{\n"
		+ "    \"nID\":150,\n"
		+ "    \"sID\":\"34125265377\",\n"
		+ "    \"sLabel\":null,\n"
		+ "    \"sLabelShort\":null\n"
		+ "}\n"
		+ noteCODE;

    private static final String noteSetSubjectHuman = noteController + "Нет описания #####\n\n";
 
    private static final String noteSetSubjectOrgan = noteController + "Нет описания #####\n\n";
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectHumanDao subjectHumanDao;

    @Autowired
    private SubjectOrganDao subjectOrganDao;

    /**
     * получение субъекта, если таков найден, или добавление субъекта в противном случае
     * @param nID ИД-номер субъекта
     * @param sINN строка-ИНН (субъект - человек)
     * @param sOKPO строка-ОКПО (субъек - организация)
     * @param nID_Subject ID авторизированого субъекта (добавляется в запрос автоматически после аутентификации пользователя)
     */
    @ApiOperation(value = "Получение субъекта", notes = noteSyncSubject )
    @RequestMapping(value = "/syncSubject", method = RequestMethod.GET, headers = { "Accept=application/json" })
    public
    @ResponseBody
    Subject syncSubject(
	    @ApiParam(value = "ИД-номер субъекта", required = false) @RequestParam(value = "nID", required = false) Long nID,
	    @ApiParam(value = "строка-ИНН (субъект - человек)", required = false) @RequestParam(required = false) String sINN,
	    @ApiParam(value = "строка-ОКПО (субъек - организация)", required = false) @RequestParam(required = false) String sOKPO,
//	    @ApiParam(value = "строка-Email (субъект - человек)", required = false) @RequestParam(required = false) String sEmail,
            HttpServletResponse httpResponse) {

        LOG.info("--- syncSubject ---");
        Subject subject = null;
        if (nID != null) {
            subject = subjectDao.getSubject(nID);
        } else if (StringUtils.isNotEmpty(sINN)) {// || StringUtils.isNotEmpty(sEmail)
            SubjectHuman oSubjectHuman = subjectHumanDao.getSubjectHuman(sINN);
            if (oSubjectHuman == null) {
                oSubjectHuman = subjectHumanDao.setSubjectHuman(sINN);
            }
            subject = oSubjectHuman.getoSubject();
        } else if (StringUtils.isNotEmpty(sOKPO)) {
            SubjectOrgan subjectOrgan = subjectOrganDao.getSubjectOrgan(sOKPO);
            if (subjectOrgan == null) {
                subjectOrgan = subjectOrganDao.setSubjectOrgan(sOKPO);
            }
            subject = subjectOrgan.getoSubject();
        } else {
            throw new ActivitiObjectNotFoundException(
                    "RequestParam not found! You should add nID or sINN or sOKPO param!", Subject.class);
        }
        if (subject == null) {
            throw new ActivitiObjectNotFoundException(
                    String.format("Subject not found! nID = %s sINN = %s sOKPO = %s", nID, sINN, sOKPO), Subject.class);
        }
        httpResponse.setHeader("Content-Type", "application/json;charset=UTF-8");
        return subject;
    }

    @ApiOperation(value = "/setSubjectHuman", notes = noteSetSubjectHuman )
    @RequestMapping(value = "/setSubjectHuman", method = RequestMethod.POST, headers = { "Accept=application/json" })
    public
    @ResponseBody
    SubjectHuman setSubject(@RequestBody SubjectHuman subjectHuman) {
        return subjectHumanDao.saveOrUpdateHuman(subjectHuman);
    }

    @ApiOperation(value = "/setSubjectOrgan", notes = noteSetSubjectOrgan )
    @RequestMapping(value = "/setSubjectOrgan", method = RequestMethod.POST, headers = { "Accept=application/json" })
    public
    @ResponseBody
    SubjectOrgan setSubject(@RequestBody SubjectOrgan subjectOrgan) {
        return subjectOrganDao.saveOrUpdateSubjectOrgan(subjectOrgan);
    }
}
