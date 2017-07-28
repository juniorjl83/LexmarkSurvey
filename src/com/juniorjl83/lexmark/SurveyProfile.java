
package com.juniorjl83.lexmark;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.ungoverned.gravity.servicebinder.Lifecycle;
import org.ungoverned.gravity.servicebinder.ServiceBinderContext;

import com.lexmark.prtapp.profile.BasicProfileContext;
import com.lexmark.prtapp.profile.PrtappProfile;
import com.lexmark.prtapp.profile.PrtappProfileException;
import com.lexmark.prtapp.profile.WelcomeScreenable;
import com.lexmark.prtapp.std.prompts.MessagePrompt;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.prompt.PromptFactory;
import com.lexmark.prtapp.prompt.PromptFactoryException;
import com.lexmark.prtapp.util.Messages;
import com.lexmark.prtapp.settings.RequiredSettingValidator;
import com.lexmark.prtapp.settings.SettingsAdmin;
import com.lexmark.prtapp.settings.SettingsStatus;
import com.lexmark.prtapp.smbclient.SmbClientService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

public class SurveyProfile implements PrtappProfile, WelcomeScreenable,
      RequiredSettingValidator, Lifecycle, ManagedService
{
   // ServiceBinder context - we can get a BundleContext from it
   private ServiceBinderContext sbc = null;
   private SettingsAdmin settingsAdmin = null;
   private SmbClientService smbClientService = null;
   private static final String icon = "/survey-icon11.png";
   private String iconText = null;
   boolean activated = false;
   private ServiceRegistration profileRegistration = null;
   private static final String CARRIAGE_RETURN = System
         .getProperty("line.separator");;
   // private SurveyProfileService _surveyprofileservice = null;

   /**
    * Constructor called by Service Binder. We need to keep track of the
    * context, since it allows us access to our own bundle.
    */
   public SurveyProfile(ServiceBinderContext sbc)
   {
      this.sbc = sbc;
   }

   public String getId()
   {
      return "SurveyProfile";
   }

   public String getName(Locale locale)
   {
      Activator.getLog().info("Seteo inicial nombre icono");
      Messages messages = new Messages("Resources", locale,
            getClass().getClassLoader());
      return messages.getString("profile.name");
   }

   public int getShortcut()
   {
      return 0;
   }

   // This only has an effect for framework 2.0 and above
   public boolean showInHeldJobsList()
   {
      return true;
   }

   public void go(BasicProfileContext context) throws PrtappProfileException
   {

      // TODO : Implement your business logic here.
      context.showPleaseWait(true);
      PromptFactory pf = context.getPromptFactory();
      MessagePrompt mp1;
      try
      {
         Messages message = new Messages("Resources", context.getLocale(),
               getClass().getClassLoader());
         String label = message.getString("prompt.label");
         mp1 = (MessagePrompt) pf.newPrompt(MessagePrompt.ID);
         mp1.setLabel(label);
         context.displayPrompt(mp1);
      }
      catch (PromptFactoryException e)
      {
         Activator.getLog().debug("Exception thrown", e);
      }
      catch (PromptException e)
      {
         Activator.getLog().debug("Exception thrown", e);
      }

   }

   /**
    * ServiceBinder method - called when SettingsAdmin arrives
    */
   public void addSettingsAdmin(SettingsAdmin svc)
   {
      settingsAdmin = svc;
   }

   /**
    * ServiceBinder method - called when SettingsAdmin leaves town
    */
   public void removeSettingsAdmin(SettingsAdmin svc)
   {
      settingsAdmin = null;
   }

   /**
    * ServiceBinder method - called when SmbClientService arrives
    */
   public void addSmbClientService(SmbClientService svc)
   {
      smbClientService = svc;
   }

   /**
    * ServiceBinder method - called when SmbClientService leaves town
    */
   public void removeSmbClientService(SmbClientService svc)
   {
      smbClientService = null;
   }

   /*
    * public void addSurveyProfileService(SurveyProfileService service) {
    * _surveyprofileservice = service;
    * 
    * }
    * 
    * public void removeSurveyProfileService(SurveyProfileService service) { //
    * This service just went away, we shouldn't rely on any // of its methods
    * still being valid.
    * 
    * _surveyprofileservice = null;
    * 
    * }
    */
   public boolean validate(String pid, Dictionary settings, Locale locale,
         SettingsStatus status)
   {
      Activator.getLog().info("Pid is " + pid);
      Messages messages = new Messages(locale, getClass().getClassLoader());
      String msg = "";
      // entra a configurar encuestas
      if (!pid.equals("survey2"))
      {
         boolean isError = false;
         boolean isValidBeginDate = false;
         boolean isValidEndDate = false;

         String beginDate = (String) settings.get("settings.instanceBegin");
         Activator.getLog()
               .info("begin date validation: " + Util.isValidDate(beginDate));
         if (!Util.isEmpty(beginDate))
         {
            if (!Util.isValidDate(beginDate))
            {
               Activator.getLog().info("entra if beginDate ");
               msg += messages.getString("setting.error.beginDate");
               msg += CARRIAGE_RETURN;
               isError = true;
            }
            else
            {
               Activator.getLog().info("entra else beginDate ");
               isValidBeginDate = true;
            }
         }

         String endDate = (String) settings.get("settings.instanceEnd");
         Activator.getLog()
               .info("end date validation: " + Util.isValidDate(endDate));
         if (!Util.isEmpty(endDate))
         {
            if (!Util.isValidDate(endDate))
            {
               Activator.getLog().info("entra if endDate ");
               msg += messages.getString("setting.error.endDate");
               msg += CARRIAGE_RETURN;
               isError = true;
            }
            else
            {
               Activator.getLog().info("entra else endDate ");
               isValidEndDate = true;
            }
         }

         if ((Util.isEmpty(beginDate) && !Util.isEmpty(endDate))
               || (!Util.isEmpty(beginDate) && Util.isEmpty(endDate)))
         {
            Activator.getLog().info("entra if bothDates ");
            msg += messages.getString("setting.error.bothDatesmustFill");
            msg += CARRIAGE_RETURN;
            isError = true;
         }

         if (isValidBeginDate && isValidEndDate)
         {
            Activator.getLog().info("entra if both dates valid ");
            String dateValidation = Util.dateValidation(beginDate, endDate);
            if ( !Util.isEmpty(dateValidation) ){
               msg += dateValidation;
               isError = true;
            }
         }

         String jsonPreguntas = (String) settings.get("settings.instanceJson");
         if (!Util.isValidJson(jsonPreguntas))
         {
            Activator.getLog().info("entra if json ");
            msg += messages.getString("setting.error.json");
            isError = true;
         }
         if (isError)
         {
            status.addStatus("setting.settingvalidationexample.error",
                  "settings.instanceJson", msg,
                  SettingsStatus.STATUS_TYPE_ERROR);
            return false;
         }
      }
      else

      {
         Activator.getLog().info("Pid padre");
         ArrayList lstServer = new ArrayList();

         String serverLog = (String) settings.get("settings.log.server");
         String sharedNameLog = (String) settings.get("settings.log.shareName");
         String domainLog = (String) settings.get("settings.log.domain");
         String pathLog = (String) settings.get("settings.log.path");
         String userIdLog = (String) settings.get("settings.network.user");
         String passwordLog = (String) settings
               .get("settings.network.password");

         if (!isEmpty(serverLog) && !isEmpty(sharedNameLog)
               && !isEmpty(userIdLog) && !isEmpty(passwordLog))
         {
            Activator.getLog().info("ENTRA A SETEAR SERVER L0G");
            lstServer.add(new InfoServer(serverLog, sharedNameLog, domainLog,
                  pathLog, userIdLog, passwordLog));

            InfoServer infoServer = new InfoServer(Activator.getLog());
            infoServer.verificarConexiones(lstServer, smbClientService);
            String msgServer = "";

            for (int i = 0; i < lstServer.size(); i++)
            {

               InfoServer server = ((InfoServer) lstServer.get(i));

               if (server.getClient() == null)
               {
                  msgServer += " No se ha podido conectar con el servidor log.";
               }
            }

            if (msgServer.length() > 0)
            {
               status.addStatus("setting.settingvalidationexample.error",
                     "settings.log.server", msgServer,
                     SettingsStatus.STATUS_TYPE_ERROR);
               return false;
            }
         }
      }
      return true;
   }

   private boolean isEmpty(String value)
   {
      Activator.getLog().info(value);
      if (value != null && value != "")
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public InputStream getDownIcon()
   {
      InputStream iconStream = getClass().getResourceAsStream(icon);
      return iconStream;
   }

   public String getIconText(Locale locale)
   {
      return iconText;
   }

   public InputStream getUpIcon()
   {
      InputStream iconStream = getClass().getResourceAsStream(icon);
      return iconStream;
   }

   public String getWorkflowOveride()
   {
      return null;
   }

   public synchronized void activate()
   {
      activated = true;
      updateIcon();

   }

   public synchronized void deactivate()
   {
      activated = false;
      updateIcon();

   }

   private void updateIcon()
   {
      if (profileRegistration != null)
      {
         profileRegistration.unregister();
         profileRegistration = null;
      }

      if (activated)
      {
         Dictionary dict = new Hashtable();
         profileRegistration = sbc.getBundleContext().registerService(
               "com.lexmark.prtapp.profile.PrtappProfile", this, dict);
      }
   }

   public void updated(Dictionary settings) throws ConfigurationException
   {
      Activator.getLog().info("entra a updated!");
      Activator.getLog().info("settings is null:: " + settings);
      if (settings != null)
      {
         boolean iconNeedsUpdate = false;

         Activator.getLog().info("We got new settings!");
         Enumeration elems = settings.keys();

         while (elems.hasMoreElements())
         {
            String key = elems.nextElement().toString();
            Object value = settings.get(key);
            Activator.getLog().info("\t" + key + " = " + value);

            if (key.equals("settings.icon.text"))
            {
               iconNeedsUpdate = true;
               iconText = (String) value;
            }
         }
         if (iconNeedsUpdate) updateIcon();
      }

   }
}
