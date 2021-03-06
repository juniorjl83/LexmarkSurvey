
package com.juniorjl83.lexmark;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.ungoverned.gravity.servicebinder.Lifecycle;
import org.ungoverned.gravity.servicebinder.ServiceBinderContext;

import com.juniorjl83.lexmark.customvlm.LikePrompt;
import com.juniorjl83.lexmark.customvlm.OmmrPrompt;
import com.juniorjl83.lexmark.customvlm.OmurPrompt;
import com.lexmark.prtapp.email.EmailConsts;
import com.lexmark.prtapp.email.EmailException;
import com.lexmark.prtapp.email.EmailMessage;
import com.lexmark.prtapp.email.EmailService;
import com.lexmark.prtapp.newcharacteristics.DeviceCharacteristicsService;
import com.lexmark.prtapp.profile.BasicNavigator;
import com.lexmark.prtapp.profile.BasicProfileContext;
import com.lexmark.prtapp.profile.PrtappProfile;
import com.lexmark.prtapp.profile.PrtappProfileException;
import com.lexmark.prtapp.profile.WelcomeScreenable;
import com.lexmark.prtapp.std.prompts.ComboPrompt;
import com.lexmark.prtapp.std.prompts.IntegerPrompt;
import com.lexmark.prtapp.std.prompts.MessagePrompt;
import com.lexmark.prtapp.std.prompts.StringPrompt;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.prompt.PromptFactory;
import com.lexmark.prtapp.prompt.PromptFactoryException;
import com.lexmark.prtapp.util.Messages;
import com.lexmark.prtapp.settings.RequiredSettingValidator;
import com.lexmark.prtapp.settings.SettingDefinitionMap;
import com.lexmark.prtapp.settings.SettingsAdmin;
import com.lexmark.prtapp.settings.SettingsGroup;
import com.lexmark.prtapp.settings.SettingsStatus;
import com.lexmark.prtapp.smbclient.AuthOptions;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.smbclient.SmbClientException;
import com.lexmark.prtapp.smbclient.SmbClientService;
import com.lexmark.prtapp.smbclient.SmbConfig.ConfigBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SurveyProfile implements PrtappProfile, WelcomeScreenable,
      RequiredSettingValidator, Lifecycle, ManagedService
{
   // ServiceBinder context - we can get a BundleContext from it
   private ServiceBinderContext sbc = null;
   private SettingsAdmin settingsAdmin = null;
   private EmailService emailService = null;
   private SmbClientService smbClientService = null;
   private static final String icon = "/survey-icon11.png";
   private byte[] iconUpImage = null;
   private String iconText = null;
   private boolean activated = false;
   private ServiceRegistration profileRegistration = null;
   private static final String CARRIAGE_RETURN = System
         .getProperty("line.separator");
   private DeviceCharacteristicsService characteristicsService = null;
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
      Activator.getLog().info("Inicio de encuestas: ");
      context.showPleaseWait(true);
      PromptFactory pf = context.getPromptFactory();
      MessagePrompt mp1;
      String filename = "default.txt";
      Log lineLog = new Log();

      try
      {
         boolean validacionInicial = true;
         
         if (validacionInicial)
         {
            Activator.getLog().info("validacion exitosa");
            SettingsGroup instances = settingsAdmin
                  .getInstanceSettings("survey2");
            Set set = instances.getInstancePids();
            Iterator i = set.iterator();
            List encuestas = new ArrayList();
            Set servicios = new HashSet();
            while (i.hasNext())
            {
               Encuesta encuesta = new Encuesta();
               String pid = (String) i.next();
               SettingDefinitionMap instance = instances.getInstance(pid);
               encuesta = Util.parseSettingToEncuestaObj(instance);
               encuesta.setPid(pid);
               servicios.add(encuesta.getServicio().toUpperCase());
               Activator.getLog().info("fecha printer::: " + new Date());
               Activator.getLog()
                     .info("inicial encuesta::: " + encuesta.getFechaInicio());
               Activator.getLog()
                     .info("final encuesta::: " + encuesta.getFechaFin());

               if ((encuesta.getFechaInicio() != null
                     && encuesta.getFechaFin() != null)
                     && new Date().after(encuesta.getFechaInicio())
                     && new Date().before(encuesta.getFechaFin()))
               {
                  Activator.getLog().info("Agrega la encuesta");
                  encuestas.add(encuesta);
               }
               else if (encuesta.getFechaInicio() == null
                     && encuesta.getFechaFin() == null)
               {
                  Activator.getLog().info("Agrega la encuesta fechas null");
                  encuestas.add(encuesta);
               }
            }
            
            List cbServicios = resolveEncuestasByServicios(servicios, encuestas);
            
            //mostrar servicios a seleccionar
            
            if (cbServicios.isEmpty())
            {
               throw new NoEncuestasActivasException();
            }
            
            Collections.sort(cbServicios, new ComparatorServicio());
            
            ArrayList namesServicios = new ArrayList();
            for (int j = 0; j < cbServicios.size(); j++)
            {
               Servicio servicio = (Servicio) cbServicios.get(j);
               namesServicios.add(servicio.getNombre());

            }
            boolean controlPantallas = true;
            int state = 0;
            String selectedPid = "";
            List targetEncuestas = new ArrayList();
            BasicNavigator navbar = (BasicNavigator)context.getNavigator();
            int selectionServicio = 0;
            SettingDefinitionMap ourAppSettings = settingsAdmin
                  .getGlobalSettings("survey2");
            loop: while (controlPantallas){
               switch(state) {
               case 0:
                  Activator.getLog().info("case 0:::");
                  navbar.showBackButton(false);
                  String[] namesAsArray = (String[]) namesServicios.toArray(new String[0]);
                  ComboPrompt cpServicio = (ComboPrompt) context.getPromptFactory()
                        .newPrompt(ComboPrompt.ID);
                  cpServicio.setItems(namesAsArray);
                  cpServicio.setLabel("Seleccione el Servicio a encuestar.");
                  cpServicio.setSelection(0);
                  if (namesAsArray.length > 1) {
                     context.displayPrompt(cpServicio);
                     selectionServicio = cpServicio.getSelection();   
                  } else {
                     selectionServicio = 0;
                  }
                  
                  navbar.showBackButton(true);
               case 1:
                  Activator.getLog().info("case 1:::");
                  Servicio servicioSeleccionado = (Servicio) cbServicios.get(selectionServicio);
                  targetEncuestas = servicioSeleccionado.getEncuestas();
                  selectedPid = "";
                  if (targetEncuestas.size() > 1)
                  {
                     //todo en base a el servicio seleccionado
                     Collections.sort(targetEncuestas, new ComparatorEncuesta());
                     ArrayList namesEncuestas = new ArrayList();
                     ArrayList pidsEncuestas = new ArrayList();
                     for (int j = 0; j < targetEncuestas.size(); j++)
                     {
                        Encuesta encuesta = (Encuesta) targetEncuestas.get(j);
                        namesEncuestas.add(encuesta.getNombre());
                        pidsEncuestas.add(encuesta.getPid());
                     }
                     
                     namesAsArray = (String[]) namesEncuestas.toArray(new String[0]);
                     ComboPrompt cpEncuesta = (ComboPrompt) context.getPromptFactory()
                           .newPrompt(ComboPrompt.ID);
                     cpEncuesta = (ComboPrompt) context.getPromptFactory()
                           .newPrompt(ComboPrompt.ID);
                     cpEncuesta.setItems(namesAsArray);
                     
                     String seleeccioneEncuesta = (String) ourAppSettings.get("msg.title.seleccione.encuesta")
                           .getCurrentValue();
                     
                     cpEncuesta.setLabel(seleeccioneEncuesta);
                     cpEncuesta.setSelection(0);
                     context.displayPrompt(cpEncuesta);
                     Activator.getLog().info(
                           "dismiis button::: " + cpEncuesta.getDismissButton());
                     if ("back".equals(cpEncuesta.getDismissButton()))
                     {
                        state = 0;
                        break;
                     }
                     
                     int selectionEncuesta = cpEncuesta.getSelection();
                     selectedPid = (String) pidsEncuestas.get(selectionEncuesta);
                  } 
                  else
                  {
                     selectedPid = ((Encuesta) targetEncuestas.get(0)).getPid();
                  }
               case 2:
                  Activator.getLog().info("case 2:::");
                  SettingDefinitionMap instance = instances
                     .getInstance(selectedPid);
                  filename = (String) instance.get("settings.log.promptName")
                        .getCurrentValue();
      
                  Encuesta encuesta = getEncuesta(selectedPid, targetEncuestas);
                  lineLog.getEncabezado().add("Equipo");
                  lineLog.getEncabezado().add("Fecha"); 
                  List preguntas = encuesta.getPreguntas();
                  lineLog = new Log();
                  String ip = characteristicsService.get("serialNumber");
                  SimpleDateFormat fecha = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm");
                  lineLog.setSerial(ip);
                  lineLog.setDate(fecha.format(new Date()));
                  boolean returnServicioEncuesta = false;
                  
                  for (int k = 0; k < preguntas.size(); k++)
                  {
                     returnServicioEncuesta = false;
                     Pregunta pregunta = (Pregunta) preguntas.get(k);
                     Activator.getLog()
                           .info("Pregunta::: " + pregunta.getPregunta());
                     lineLog.getEncabezado().add(pregunta.getPregunta());
                     List opciones = pregunta.getOpciones();
      
                     if ("omur".equals(pregunta.getTipo()))
                     {
                        OmurPrompt omurPromt = new OmurPrompt(
                              String.valueOf(pregunta.getId()),
                              pregunta.getPregunta(), opciones);
                        context.displayPrompt(omurPromt);
                        Activator.getLog().info(
                              "dismiis button::: " + omurPromt.getDismissButton());
                        if ("cancel".equals(omurPromt.getDismissButton()))
                        {
                           throw new PromptException(
                                 PromptException.PROMPT_CANCELLED_BY_USER);
                        }else if ("back".equals(omurPromt.getDismissButton())){
                           Activator.getLog().info("pregunta:::" + k);
                           if (k - 1 < 0){
                              Activator.getLog().info("devuelve antes encuesta:::");
                              if (targetEncuestas.size() > 1){
                                 state = 1;
                              }else{
                                 state = 0;
                              }
                              returnServicioEncuesta = true;
                              break;
                           }else{
                              Activator.getLog().info("remueve ultima respuesta:::");
                              removeUltimaRespuesta(lineLog);
                              k = k - 2;
                              continue;
                           }
                        }else{
                           lineLog.getRespuestas().add(omurPromt.getRespuesta());
                           Activator.getLog().info(
                                 "omur respuesta::: " + omurPromt.getRespuesta());                           
                        }
                     }
                     else if ("ommr".equals(pregunta.getTipo()))
                     {
                        OmmrPrompt ommrPromt = new OmmrPrompt(
                              String.valueOf(pregunta.getId()),
                              pregunta.getPregunta(), opciones);
                        context.displayPrompt(ommrPromt);
                        Activator.getLog().info(
                              "dismiis button::: " + ommrPromt.getDismissButton());
                        if ("cancel".equals(ommrPromt.getDismissButton()))
                        {
                           throw new PromptException(
                                 PromptException.PROMPT_CANCELLED_BY_USER);
                        }else if ("back".equals(ommrPromt.getDismissButton())){
                           Activator.getLog().info("pregunta:::" + k);
                           if (k - 1 < 0){
                              Activator.getLog().info("devuelve antes encuesta:::");
                              if (targetEncuestas.size() > 1){
                                 state = 1;
                              }else{
                                 state = 0;
                              }
                              returnServicioEncuesta = true;
                              break;
                           }else{
                              Activator.getLog().info("remueve ultima respuesta:::");
                              removeUltimaRespuesta(lineLog);
                              k = k - 2;
                              continue;
                           }
                        }else{
                           lineLog.getRespuestas().add(ommrPromt.getRespuesta());
                           Activator.getLog().info(
                                 "ommr respuesta::: " + ommrPromt.getRespuesta());
                        }
                     }
                     else if ("texto".equals(pregunta.getTipo()))
                     {
                        StringPrompt texto = (StringPrompt) context
                              .getPromptFactory().newPrompt(StringPrompt.ID);
                        texto.setName("texto");
                        texto.setLabel(pregunta.getPregunta());
                        texto.setMinLength(5);
                        context.displayPrompt(texto);
                        if ("back".equals(texto.getDismissButton()))
                        {
                           Activator.getLog().info("pregunta:::" + k);
                           if (k - 1 < 0){
                              Activator.getLog().info("devuelve antes encuesta:::");
                              if (targetEncuestas.size() > 1){
                                 state = 1;
                              }else{
                                 state = 0;
                              }
                              returnServicioEncuesta = true;
                              break;
                           }else{
                              Activator.getLog().info("remueve ultima respuesta:::");
                              removeUltimaRespuesta(lineLog);
                              k = k - 2;
                              continue;
                           }
                        }else{
                           lineLog.getRespuestas().add(texto.getValue().replace(',', ' '));   
                        }
                     }
                     else if ("like".equals(pregunta.getTipo()))
                     {
                        LikePrompt likePromt = new LikePrompt(String.valueOf(pregunta.getId()),
                              pregunta.getPregunta());
                        context.displayPrompt(likePromt);
                        Activator.getLog().info(
                              "dismiis button::: " + likePromt.getDismissButton());
                        if ("cancel".equals(likePromt.getDismissButton()))
                        {
                           throw new PromptException(
                                 PromptException.PROMPT_CANCELLED_BY_USER);
                        }else if ("back".equals(likePromt.getDismissButton())){
                           Activator.getLog().info("pregunta:::" + k);
                           if (k - 1 < 0){
                              Activator.getLog().info("devuelve antes encuesta:::");
                              if (targetEncuestas.size() > 1){
                                 state = 1;
                              }else{
                                 state = 0;
                              }
                              returnServicioEncuesta = true;
                              break;
                           }else{
                              Activator.getLog().info("remueve ultima respuesta:::");
                              removeUltimaRespuesta(lineLog);
                              k = k - 2;
                              continue;
                           }
                        }else{
                           lineLog.getRespuestas().add(likePromt.getRespuesta());
                           Activator.getLog().info(
                                 "like respuesta::: " + likePromt.getRespuesta());   
                        }
                     }
                     else if ("numerico".equals(pregunta.getTipo()))
                     {
                        IntegerPrompt integer = (IntegerPrompt) context
                              .getPromptFactory().newPrompt(IntegerPrompt.ID);
                        integer.setName("Numerico");
                        integer.setLabel(pregunta.getPregunta());
                        integer.setMinValue(Long.parseLong(pregunta.getMinimo()));
                        integer.setMaxValue(Long.parseLong(pregunta.getMaximo()));
                        context.displayPrompt(integer);
                        if ("back".equals(integer.getDismissButton()))
                        {
                           Activator.getLog().info("pregunta:::" + k);
                           if (k - 1 < 0){
                              Activator.getLog().info("devuelve antes encuesta:::");
                              if (targetEncuestas.size() > 1){
                                 state = 1;
                              }else{
                                 state = 0;
                              }
                              returnServicioEncuesta = true;
                              break;
                           }else{
                              Activator.getLog().info("remueve ultima respuesta:::");
                              removeUltimaRespuesta(lineLog);
                              k = k - 2;
                              continue;
                           }
                        }else{
                           lineLog.getRespuestas().add(String.valueOf(integer.getValue()));   
                        }
                     }
                     else
                     {
                     }
                  }//end for preguntas
                  if (returnServicioEncuesta){
                     break;
                  }
               default:
                  break loop; 
               }//end switch
            }//end loop
           
            String logCharacter = (String) ourAppSettings
                  .get("settings.log.caracter").getCurrentValue();
            lineLog.setSeparator(logCharacter);
            String shareName = (String) ourAppSettings
                  .get("settings.log.shareName").getCurrentValue();
            String serverAddress = (String) ourAppSettings
                  .get("settings.log.server").getCurrentValue();
            String initialPath = (String) ourAppSettings
                  .get("settings.log.path").getCurrentValue();
            String domainLog = (String) ourAppSettings
                  .get("settings.log.domain").getCurrentValue();
            String userName = (String) ourAppSettings
                  .get("settings.network.user").getCurrentValue();
            String password = (String) ourAppSettings
                  .get("settings.network.password").getCurrentValue();
            String email = (String) ourAppSettings.get("settings.log.email")
                  .getCurrentValue();
            
            ConfigBuilder configBuilder = smbClientService
                  .getSmbConfigBuilder();
            configBuilder.setAuthType(AuthOptions.NTLMv2);
            configBuilder.setServer(serverAddress);
            configBuilder.setShare(shareName);
            configBuilder.setPath(initialPath);
            configBuilder.setUserId(userName);
            configBuilder.setPassword(password);

            if (domainLog != null && domainLog.length() > 0)
            {
               configBuilder.setDomain(domainLog);
            }

            SmbClient clientLog = null;
            try
            {
               Activator.getLog().info("antes de escribir el log");
               Activator.getLog()
                     .info("Log a escribir::: " + lineLog.toString());
               clientLog = smbClientService
                     .getNewSmbClient(configBuilder.build());
               WriteLog wl = new WriteLog(clientLog, Activator.getLog(),
                     filename, lineLog.toString(), lineLog.toStringEncabezado());
               wl.start();
               
               if(emailService != null && email.length() > 0)
               {
                  try
                  {
                     String asunto = (String) ourAppSettings.get("settings.log.subject")
                           .getCurrentValue();
                     Activator.getLog().info("email: " + email);   
                     Activator.getLog().info("asunto: " + asunto);
                     EmailMessage em = emailService.newEmailMessage(email, asunto, lineLog.toString());
                     em.setConnectionTest();
                     int success = emailService.send(em);
                     Activator.getLog().info("envio correo " + success);   
                  }
                  catch (EmailException e)
                  {
                     Activator.getLog().info("error envio correo msg " + e.getMessage());  
                  }
                  
               }else{
                  Activator.getLog().info("servicio correo no disponible");
               }
            }
            catch (com.lexmark.prtapp.smbclient.ConfigurationException e)
            {
               Activator.getLog().info("err abre lg ");
               e.printStackTrace();
            }
            catch (SmbClientException e)
            {
               Activator.getLog().info("err abre lg ");
               e.printStackTrace();
            }
            
            Activator.getLog().info("fin escribir log");
            navbar.showBackButton(false);
            Messages message = new Messages("Resources", context.getLocale(),
                  getClass().getClassLoader());
            String label = (String) ourAppSettings.get("msg.title.muchas.gracias")
                  .getCurrentValue();
            mp1 = (MessagePrompt) pf.newPrompt(MessagePrompt.ID);
            mp1.setLabel(label);
            context.displayPrompt(mp1);

            Activator.getLog().info("fin mostrar pantalla");

            Activator.getLog().info("ejecucion programa");
         }
         else
         {
            MessagePrompt noInstances = (MessagePrompt) context
                  .getPromptFactory().newPrompt(MessagePrompt.ID);
            noInstances.setMessage(
                  "Debe configurarse el log y por lo menos una encuesta antes de ingresar!");
            context.displayPrompt(noInstances);
         }
      }
      catch (NoEncuestasActivasException e)
      {
         MessagePrompt noInstances;
         try
         {
            noInstances = (MessagePrompt) context.getPromptFactory()
                  .newPrompt(MessagePrompt.ID);
            noInstances.setMessage(
                  "No existen encuestas activas. Contacte al administrador.!");
            context.displayPrompt(noInstances);
         }
         catch (PromptFactoryException e1)
         {
            Activator.getLog().debug("Exception thrown", e);
         }
         catch (PromptException e1)
         {
            Activator.getLog().debug("Exception thrown", e);
         }

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

   private void removeUltimaRespuesta(Log lineLog)
   {
      int size = lineLog.getRespuestas().size();
      lineLog.getRespuestas().remove(size-1);
      
   }

   private List resolveEncuestasByServicios(Set servicios, List encuestas)
   {
      List cbServicios = new ArrayList();
      Iterator iteratorServ = servicios.iterator();
      
      while (iteratorServ.hasNext()){
         Servicio servicio = new Servicio();
         servicio.setNombre((String)iteratorServ.next());
         for (int i=0; i< encuestas.size(); i++){
            Encuesta encuesta = (Encuesta) encuestas.get(i);
            if (servicio.getNombre().equalsIgnoreCase(encuesta.getServicio())){
               servicio.getEncuestas().add(encuesta);
            }
         }
         if ( servicio.getEncuestas().size() > 0 ){
            cbServicios.add(servicio);
         }
      }
      
      return cbServicios;
   }

   private String getRespuestas(int[] opcionesArray, List opciones)
   {
      StringBuffer respuestas = new StringBuffer();
      for (int i = 0; i < opcionesArray.length; i++)
      {
         Opcion opcion = (Opcion) opciones.get(opcionesArray[i]);
         respuestas.append(opcion.getValor());
         if (i < opcionesArray.length - 1)
         {
            respuestas.append("-");
         }
      }
      return respuestas.toString();
   }

   private String[] getOpciones(List opciones)
   {
      String[] opcionesArray = new String[opciones.size()];
      for (int i = 0; i < opciones.size(); i++)
      {
         Opcion opcion = (Opcion) opciones.get(i);
         opcionesArray[i] = opcion.getDescripcion();
      }
      return opcionesArray;
   }

   private Encuesta getEncuesta(String selectedPid, List encuestas)
   {
      for (int i = 0; i < encuestas.size(); i++)
      {
         Encuesta encuesta = (Encuesta) encuestas.get(i);
         if (selectedPid.equals(encuesta.getPid()))
         {
            return encuesta;
         }
      }
      return null;
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
    * Called when the e-mail service arrives.
    */
   public void addEmailService(EmailService svc)
   {
      emailService = svc;
   }
   
   /**
    * Called when the e-mail service goes away.
    */
   public void removeEmailService(EmailService svc)
   {
      emailService = null;
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
            if (!Util.isEmpty(dateValidation))
            {
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

         // validacion estructura json
         ValidacionJson validacionJson = Util
               .validateJsonStructure(jsonPreguntas);
         if (validacionJson.isError())
         {
            msg += validacionJson.getMsgValidacion();
            isError = validacionJson.isError;
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
      if(iconUpImage == null || iconUpImage.length == 0)
      {
         InputStream iconStream = getClass().getResourceAsStream(icon);
         return iconStream;
      }
      else
      {
         return new ByteArrayInputStream(iconUpImage);
      }
      
   }

   public String getIconText(Locale locale)
   {
      return iconText;
   }

   public InputStream getUpIcon()
   {
      if(iconUpImage == null || iconUpImage.length == 0)
      {
         InputStream iconStream = getClass().getResourceAsStream(icon);
         return iconStream;
      }
      else
      {
         return new ByteArrayInputStream(iconUpImage);
      }
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
            }else if(key.equals("settings.icon.image"))
            {
               iconNeedsUpdate = true;
               iconUpImage = (byte[])value;
            }
         }
         if (iconNeedsUpdate) updateIcon();
      }
   }

   /**
    * ServiceBinder method - called when DeviceCharacteristicsService arrives
    */
   public void addDeviceCharacteristics(DeviceCharacteristicsService svc)
   {
      characteristicsService = svc;
   }

   /**
    * ServiceBinder method - called when DeviceCharacteristicsService leaves
    * town
    */
   public void removeDeviceCharacteristics(DeviceCharacteristicsService svc)
   {
      characteristicsService = null;
   }
}
