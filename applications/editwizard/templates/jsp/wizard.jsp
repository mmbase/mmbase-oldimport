<%@ include file="settings.jsp"
%><mm:cloud name="mmbase" method="http" jspvar="cloud"><%@ page errorPage="exception.jsp"
%><mm:log jspvar="log"><%
    /**
     * wizard.jsp
     *
     * @since    MMBase-1.6
     * @version  $Id: wizard.jsp,v 1.5 2002-05-22 13:50:36 pierre Exp $
     * @author   Kars Veling
     * @author   Michiel Meeuwissen
     */

Config.WizardConfig wizardConfig = null;
if (ewconfig.subObjects.size() > 0) {
    if (ewconfig.subObjects.peek() instanceof Config.WizardConfig) {
        log.debug("checking configuration");
        wizardConfig = (Config.WizardConfig) ewconfig.subObjects.peek();
        Config.WizardConfig checkConfig = new Config.WizardConfig();
        log.trace("checkConfig" + configurator);
        configurator.config(checkConfig);
        if (checkConfig.objectNumber != null && (!checkConfig.objectNumber.equals(wizardConfig.objectNumber))) {
            log.debug("found wizard is for other other object (" + checkConfig.objectNumber + "!= " + wizardConfig.objectNumber + ")");
            wizardConfig = null;
        } else {
            if (closedObject instanceof Config.WizardConfig) {
                // we move from a inline sub-wizard to a parent wizard...
                Config.WizardConfig inlineWiz=(Config.WizardConfig)closedObject;
                // with an inline popupwizard we should like to pass the newly created or updated
                // item to the 'lower' wizard.
                String parentFid = inlineWiz.parentFid;
                String parentDid = inlineWiz.parentDid;
                String objnr=inlineWiz.objectNumber;
                if ("new".equals(objnr)) {
                    // obtain new object number
                    objnr=inlineWiz.wiz.getObjectNumber();
                    WizardCommand wc = new WizardCommand("cmd/add-item/"+parentFid+"/"+parentDid+"//", objnr);
                    wizardConfig.wiz.processCommand(wc);
                } else {
log.info("send cmd/update-item/"+parentFid+"/"+objnr+"//");
                    WizardCommand wc = new WizardCommand("cmd/update-item/"+parentFid+"///",objnr);
                    wizardConfig.wiz.processCommand(wc);
                }
            }
            log.debug("processing request");
            wizardConfig.wiz.processRequest(request);
        }
    }
}

if (wizardConfig == null) {
    log.trace("creating new wizard");
    wizardConfig =  configurator.createWizard(cloud);
    wizardConfig.parentFid=request.getParameter("fid");
    wizardConfig.parentDid=request.getParameter("did");
    log.trace("putting new wizard on the stack for object " + wizardConfig.objectNumber);
    ewconfig.subObjects.push(wizardConfig);
}

if (wizardConfig.wiz.mayBeClosed()) {
    log.trace("Closing this wizard");
    response.sendRedirect(response.encodeURL("wizard.jsp?sessionkey="+ewconfig.sessionKey+"&proceed=true&remove=true"));
} else {
    log.trace("Send html back");
    wizardConfig.wiz.writeHtmlForm(out, ewconfig.wizard);
}
%>
</mm:log></mm:cloud>

