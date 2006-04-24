package nl.leocms.evenementen;

import java.util.*;
import org.mmbase.bridge.*;
import org.mmbase.module.core.*;
import org.mmbase.util.logging.*;
import com.finalist.mmbase.util.CloudFactory;
import nl.leocms.evenementen.forms.SubscribeAction;
import nl.leocms.util.DoubleDateNode;
import javax.servlet.*;
import nl.mmatch.NatMMConfig;

/**
 * Created by Henk Hangyi (MMatch)
 */

public class EventNotifier implements Runnable {

   private static final Logger log = Logging.getLoggerInstance(EventNotifier.class);
   
   private static String FULLYBOOKED_EVENT = "Volgeboekte activiteit";
   private static String LESSTHANMIN_EVENT = "Activiteit met minder dan het minimum aantal deelnemers";

   public String getEventMessage(Node thisEvent, String eventMessage, String type) {
      String newline = "<br/>";
      if(type.equals("plain")) { newline = "\n"; }
      String message = "Beste CAD contactpersoon," + newline + newline 
               + "De activiteit " + thisEvent.getStringValue("titel") + " op " + (new DoubleDateNode(thisEvent)).getReadableValue()
               + " " + eventMessage + newline + newline 
               + "Dit bericht is automatisch door het CAD gegenereerd.";
      return message;
   }

   public int sendEventNotification(Cloud cloud, Node thisEvent, String eventType, String eventMessage) {
      
      String fromEmailAddress = NatMMConfig.fromCADAddress;
      String emailSubject = eventType + " " + thisEvent.getStringValue("titel") + ", " + (new DoubleDateNode(thisEvent)).getReadableValue();
      int nEmailSend = 0;

      Node emailNode = cloud.getNodeManager("email").createNode();
      emailNode.setValue("from", fromEmailAddress);
      emailNode.setValue("subject", emailSubject);
      emailNode.setValue("replyto", fromEmailAddress);
      emailNode.setValue("body",
                      "<multipart id=\"plaintext\" type=\"text/plain\" encoding=\"UTF-8\">"
                         + getEventMessage(thisEvent, eventMessage, "plain")
                      + "</multipart>"
                      + "<multipart id=\"htmltext\" alt=\"plaintext\" type=\"text/html\" encoding=\"UTF-8\">"
                      + "<html>"
                        + getEventMessage(thisEvent, eventMessage, "html") + "</html>"
                      + "</multipart>");
      emailNode.commit();
       
      String emailField = "medewerkers.email";
      NodeIterator uNodes= cloud.getList(thisEvent.getStringValue("number")
         , "evenement,related,medewerkers"
         , emailField, null, null, null, null, true).nodeIterator();  
      if(!uNodes.hasNext()) {
         emailField = "users.emailadres";
         uNodes= cloud.getList(thisEvent.getStringValue("number")
            , "evenement,schrijver,users"
            , emailField, null, null, null, null, true).nodeIterator();
      }
      while(uNodes.hasNext()) {
         String emailAddress = uNodes.nextNode().getStringValue(emailField);
         if(emailAddress!=null&&!emailAddress.equals("")) {
            emailNode.setValue("to", emailAddress);
            emailNode.commit();
            emailNode.getValue("mail(oneshotkeep)");
            nEmailSend++;
         }
      }

      thisEvent.createRelation(emailNode,cloud.getRelationManager("related")).commit();
      return nEmailSend;
   }

   public static void updateAppAttributes(Cloud cloud) {
      // *** updating the application attributes
      MMBaseContext mc = new MMBaseContext();
      ServletContext application = mc.getServletContext();
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      long lDateSearchFrom = (cal.getTime().getTime()/1000);
      String eventUrl = "from_day="+cal.get(Calendar.DAY_OF_MONTH)+"&from_month="+(cal.get(Calendar.MONTH)+1)+"&from_year="+cal.get(Calendar.YEAR);      
      cal.add(Calendar.YEAR,1); // cache events for one year from now
      long lDateSearchTill = (cal.getTime().getTime()/1000);
      eventUrl += "&till_day="+cal.get(Calendar.DAY_OF_MONTH)+"&till_month="+(cal.get(Calendar.MONTH)+1)+"&till_year="+cal.get(Calendar.YEAR);

      application.setAttribute("events",Evenement.getEvents(cloud,lDateSearchFrom,lDateSearchTill));
      application.setAttribute("events_url",eventUrl);
      application.setAttribute("events_till",new Long(lDateSearchTill));

      log.info("updated applications attributes");
   }

   public String checkOnEventsWithoutLocation(Cloud cloud) {
      // *** updating the application attributes
      String logMessage = "";
      int nUpdated = 0;
      int nSuccesfullUpdated = 0;
      NodeIterator iNodes= cloud.getList(null,"evenement","evenement.number","evenement.lokatie =',-1,'", null, null, null, false).nodeIterator();
      while(iNodes.hasNext()) {
          Node nextNode = iNodes.nextNode();
          String thisEvent = nextNode.getStringValue("evenement.number");
          cloud.getNode(thisEvent).commit();
          nUpdated++;
          if(!cloud.getNode(thisEvent).getStringValue("lokatie").equals(",-1,")) {
            nSuccesfullUpdated++;
          }
      }
      logMessage += "\n<br>Number of succesfull updated locations " + nSuccesfullUpdated + " / " + nUpdated;
      return logMessage;
   }

   public String notifyParticipants(Cloud cloud) { 
   
      String logMessage = "";
      int nEmailSend = 0;
      try {   
         // list all the subscription:
         // - who booked for an event more than one month ago,
         // - and the event is now less than one week ahead,
         // - the participants did not receive a reminder email.
         long now = (new Date().getTime())/1000;
         long one_day = 24*60*60;
         long one_week = 7*one_day;
         long one_month = 31*one_day;
         NodeIterator iNodes= cloud.getList(null
            , "evenement,posrel,inschrijvingen"
            , "inschrijvingen.number"
            , "inschrijvingen.datum_inschrijving < '" + (now - one_month) + "' AND evenement.begindatum > '" + now + "' AND evenement.begindatum < '" + (now + one_week) + "'"
            , null, null, null, false).nodeIterator();
         while(iNodes.hasNext()) {
             Node nextNode = iNodes.nextNode();
             String thisSubscription = nextNode.getStringValue("inschrijvingen.number");
             if(!Evenement.isGroupSubscription(cloud,thisSubscription)
                && cloud.getList(thisSubscription
                   , "inschrijvingen,related,email"
                   , null
                   , "email.subject LIKE 'Herinnering aanmelding %'"
                   , null, null, null, false).isEmpty()) {
               SubscribeAction.sendReminderEmail(cloud, thisSubscription);
               nEmailSend++;
            }
         }   
      } catch(Exception e) {
         log.info(e);
      }
      logMessage += "\n<br>Number of reminders send " + nEmailSend;
      return logMessage;
   }

   public void isCanceledNotification(Cloud cloud, String sEvent) {      
      int nEmailSend = sendEventNotification(cloud, cloud.getNode(sEvent), "Geannuleerde activiteit"," is geannuleerd.");  ;
   }


   public String lessThanMin(Cloud cloud) { 
   
      String logMessage = "";
      int nEmailSend = 0;
      
      try {
         
         // list all the events:
         // - the event is now less than one week ahead,
         // - and the minimum number of participants is not reached
         long now = (new Date().getTime())/1000;
         long one_day = 24*60*60;
         
         NodeIterator eNodes = cloud.getNodeManager("evenement").getList(
                                    "begindatum > '" + now + "'"
                                    + " AND begindatum < '" + (now + 2*one_day) + "'"
                                    + " AND cur_aantal_deelnemers < min_aantal_deelnemers",
                                    "begindatum", 
                                    "UP").nodeIterator();
         while(eNodes.hasNext()) {
            Node childEvent = eNodes.nextNode();
            if(cloud.getList(childEvent.getStringValue("number")
                   , "evenement,related,email"
                   , null
                   , "email.subject LIKE '" + LESSTHANMIN_EVENT + "%'"
                   , null, null, null, false).isEmpty()) {
               nEmailSend += sendEventNotification(cloud, childEvent, LESSTHANMIN_EVENT, 
                  " vindt over twee dagen plaats, maar heeft minder dan het minimum aantal deelnemers.");
            }
         }
      } catch(Exception e) {
         log.info(e);
      }
      logMessage += "\n<br>Number of 'less than minimum' notifications send " + nEmailSend;
      return logMessage;
   }

   public String isFullyBooked(Cloud cloud) { 
   
      String logMessage = "";
      int nEmailSend = 0;
      
      try {
         
         // list all the events:
         // - that are fully booked,
         // - and no notification email was send
         long now = (new Date().getTime())/1000;
         NodeIterator eNodes = cloud.getNodeManager("evenement").getList("begindatum > '" + now + "'", "begindatum", "UP").nodeIterator();
         while(eNodes.hasNext()) {
             Node childEvent = eNodes.nextNode();
             String sChild = childEvent.getStringValue("number");
             String sParent = Evenement.findParentNumber(sChild);
             Node parentEvent = cloud.getNode(sParent);
             if(Evenement.isFullyBooked(parentEvent, childEvent)) {
               if(cloud.getList(sChild
                      , "evenement,related,email"
                      , null
                      , "email.subject LIKE '" + FULLYBOOKED_EVENT + "%'"
                      , null, null, null, false).isEmpty()) {
                  nEmailSend += sendEventNotification(cloud, childEvent, FULLYBOOKED_EVENT," is volgeboekt. Indien aanwezig, open een reservedatum.");
               }
            }
         }
      } catch(Exception e) {
         log.info(e);
      }
      logMessage += "\n<br>Number of 'fully booked' notifications send " + nEmailSend;
      return logMessage;
   }  

   public void updateEventDB() { 
   
      Cloud cloud = CloudFactory.getCloud();
      MMBaseContext mc = new MMBaseContext();
      ServletContext application = mc.getServletContext();
      String requestUrl = (String) application.getAttribute("request_url");
      if(requestUrl==null) { requestUrl = ""; }

      String emailSubject = "Notificatie van " + requestUrl;

      String toEmailAddress = NatMMConfig.toEmailAddress;
      String fromEmailAddress = NatMMConfig.fromEmailAddress; 
      String liveUrl = NatMMConfig.liveUrl;

      log.info("Started updateEventDB");
      String logMessage =  "\n<br>Started updateEventDB " + new Date();

      // use liveUrl to make sure that notifications are only send from live server
      if(requestUrl.indexOf(liveUrl)>-1) {
         logMessage += notifyParticipants(cloud);
         logMessage += lessThanMin(cloud);
         logMessage += isFullyBooked(cloud);
      } else {
         logMessage += "\n<br>'" + requestUrl + "' does not match with '" + liveUrl + "' therefore no reminder emails send";
         log.info("'" + requestUrl + "' does not match with '" + liveUrl + "' therefore no reminder emails send");
      }

      updateAppAttributes(cloud);
      logMessage += "\n<br>Updated application attributes";
      logMessage += checkOnEventsWithoutLocation(cloud);
      
      logMessage += "\n<br>Finished updateEventDB " + new Date();
      log.info("Finished updateEventDB");
      
      Node emailNode = cloud.getNodeManager("email").createNode();
      emailNode.setValue("to", toEmailAddress);
      emailNode.setValue("from", fromEmailAddress);
      emailNode.setValue("subject", emailSubject);
      emailNode.setValue("replyto", fromEmailAddress);
      emailNode.setValue("body","<multipart id=\"plaintext\" type=\"text/plain\" encoding=\"UTF-8\"></multipart>"
                      + "<multipart id=\"htmltext\" alt=\"plaintext\" type=\"text/html\" encoding=\"UTF-8\">"
                      + "<html>" + logMessage + "</html>"
                      + "</multipart>");
      emailNode.commit();
      emailNode.getValue("mail(oneshot)");
   }
   
   private Thread getKicker(){
      Thread  kicker = Thread.currentThread();
      if(kicker.getName().indexOf("EventNotifierThread")==-1) {
         kicker.setName("EventNotifierThread / " + (new Date()));
         kicker.setPriority(Thread.MIN_PRIORITY+1); // *** does this help ?? ***
      }
      return kicker;
   }
   
   public EventNotifier() {
      Thread kicker = getKicker();
      log.info("EventNotifier(): " + kicker);
   }
   
   public void run () {
      Thread kicker = getKicker();
      log.info("run(): " + kicker); 
      updateEventDB();
   }
}