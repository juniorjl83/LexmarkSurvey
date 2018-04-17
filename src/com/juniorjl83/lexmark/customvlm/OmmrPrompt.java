package com.juniorjl83.lexmark.customvlm;

import java.util.List;

import com.juniorjl83.lexmark.Activator;
import com.juniorjl83.lexmark.Opcion;
import com.lexmark.core.Element;
import com.lexmark.core.ElementIterator;
import com.lexmark.core.ListElem;
import com.lexmark.prtapp.profile.VlmlNavigator;
import com.lexmark.prtapp.prompt.DisplayType;
import com.lexmark.prtapp.prompt.PromptEventResult;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.prompt.VlmlPrompt;
import com.lexmark.prtapp.prompt.VlmlPromptContext;

public class OmmrPrompt implements VlmlPrompt
{

   private String pregunta;
   private String idPregunta;
   private List opciones;
   private boolean exit = false;
   private ListElem respuesta;
   
   /** We need to keep track of the VLML prompt context ourselves here */
   VlmlPromptContext context = null;
   /** Name of the top level VLML layout */
   String vlmlName = "HardPrompt";
   /** Keep track of how we are dismissed so the profile can know */
   String dismissButton = "";
   
   public OmmrPrompt(String idPregunta, String pregunta, List opciones)
   {
      this.idPregunta = idPregunta;
      this.pregunta = pregunta;
      this.opciones = opciones;
   }
   
   public String getDismissButton()
   {
      return dismissButton;
   }

   
   public DisplayType getDisplayType()
   {
      return DisplayType.VLML;
   }

   
   public String getHelp()
   {
      return null;
   }

   
   public String getId()
   {
      return "Ommr-pregunta-" + idPregunta;
   }

   
   public String getLabel()
   {
      return null;
   }

   
   public String getName()
   {
      return vlmlName;
   }

   
   public void setHelp(String arg0)
   {
      
   }

   
   public void setLabel(String arg0)
   {
      
   }

   
   public void setName(String name)
   {
      this.vlmlName = name;
   }

   
   public void dismissed()
   {
      
   }

   
   public String getVlml()
   {
      StringBuffer screen = new StringBuffer();
      screen.append("<?xml version=\"1.0\"?>\n");
      screen.append("<GridLayout distribution=\"heterogeneous\" spacing=\"0\" columns=\"1\" rows=\"4\" name=\"ommr\">\n");
      screen.append(" <AttachChild bottom=\"1\" top=\"0\" right=\"1\" left=\"0\" yPadding=\"4\" xPadding=\"4\" yGFill=\"shrink\" xGFill=\"expandFill\">\n");
      screen.append("     <Label name=\"title\" justification=\"center\" text=\"" + pregunta + "\" style=\"bold\" size=\"medium\"/>\n");
      screen.append(" </AttachChild>\n");
      screen.append(" <AttachChild bottom=\"2\" top=\"1\" right=\"1\" left=\"0\" yPadding=\"0\" xPadding=\"0\" yGFill=\"expand\" xGFill=\"expandShrink\">\n");
      screen.append("     <SelectionGroup name=\"opciones\" numVisible=\"4\" orientation=\"vertical\">\n");
      screen.append("         <Defaults>0</Defaults>\n");
      
      for (int i = 0; i < opciones.size(); i++){
         Opcion opcion = (Opcion) opciones.get(i);
         screen.append("       <Selection name=\"" + opcion.getNumero() + "\">\n");
         screen.append("            <DeselectedImage imageName=\"listCheckboxUp\"/>\n");
         screen.append("            <SelectedImage imageName=\"listCheckboxDown\"/>\n");
         screen.append("            <Label justification=\"center\" text=\"" + opcion.getDescripcion() + "\"/>\n");
         screen.append("       </Selection>\n");
      }
      screen.append("      </SelectionGroup>\n");
      screen.append(" </AttachChild>\n");
      screen.append(" <AttachChild bottom=\"3\" top=\"2\" right=\"1\" left=\"0\" yPadding=\"0\" xPadding=\"0\" yGFill=\"fill\" xGFill=\"expandFill\">\n");
      screen.append("     <Image name=\"separatorLine\" imageName=\"NavRowBottomLine\"/>\n");
      screen.append(" </AttachChild>\n");
      
      screen.append(" <AttachChild bottom=\"4\" top=\"3\" right=\"1\" left=\"0\" yGFill=\"fill\" xGFill=\"expandFill\">\n");
      
      screen.append("     <BoxLayout name=\"navBar\" orientation=\"horizontal\" color=\"b2b2b2\">\n");
      
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">\n");
      screen.append("             <LabeledImageButton name=\"next\" overlayStyle=\"bold\" overlayPointSize=\"16\">\n");
      screen.append("                 <Normal text=\"Siguiente\" imageName=\"navRowOptionsUp\"/>\n");
      screen.append("                 <Selected text=\"Siguiente\" imageName=\"navRowOptionsDown\"/>\n");
      screen.append("             </LabeledImageButton>\n");
      screen.append("         </AttachChildToTheEnd>\n");
      
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">\n");
      screen.append("             <LabeledImageButton name=\"back\" overlayStyle=\"bold\" overlayPointSize=\"16\">\n");
      screen.append("                 <Normal text=\"Atrás\" imageName=\"navRowOptionsUp\"/>\n");
      screen.append("                 <Selected text=\"Atrás\" imageName=\"navRowOptionsDown\"/>\n");
      screen.append("             </LabeledImageButton>\n");
      screen.append("         </AttachChildToTheEnd>\n");
      
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">\n");
      screen.append("             <LabeledImageButton name=\"cancel\">\n");
      screen.append("                 <Normal imageName=\"homeUp\"/>\n");
      screen.append("                 <Selected imageName=\"homeDown\"/>\n");
      screen.append("             </LabeledImageButton>\n");
      screen.append("         </AttachChildToTheEnd>\n");
      screen.append("     </BoxLayout>\n");
      
      screen.append(" </AttachChild>\n");
      screen.append("</GridLayout>\n");
      
      return screen.toString();
   }

   
   public PromptEventResult handleEvent(String component, String event, Element data)
         throws PromptException
   {
      PromptEventResult result = null;
      Activator.getLog().info("EasyPrompt: got a VLML event!\n\tComponent = " + component + "\n\tEvent = " + event + "\n\t" + data.print("Data"));
                             
      if ( component.equals("ommr.navBar.next") ){
         Activator.getLog().info("entra a next");
         dismissButton = "next";
         if ( respuesta != null && getRespuesta().length() > 0){
            result = PromptEventResult.VALIDATE;   
         }else{
            result = PromptEventResult.CONTINUE;
         }
      }else if ( component.equals("ommr.navBar.back") ){
         Activator.getLog().info("entra a back");
         dismissButton = "back";
         result = PromptEventResult.VALIDATE;
      }else if (component.equals("ommr.navBar.cancel")){
         Activator.getLog().info("entra a cancel");
         dismissButton = "cancel";
         result = PromptEventResult.VALIDATE;
      }else if ( component.equals("ommr.opciones") ){
         Activator.getLog().info("entra a " + component);
         dismissButton = component;
         respuesta = (ListElem)data;
         result = PromptEventResult.CONTINUE;
      }else{
         Activator.getLog().info("entra a else");
         result = PromptEventResult.CONTINUE;
      }
      
      return result;
   }

   
   public void init(VlmlPromptContext promptContext, VlmlNavigator navigator)
   {
      this.context = promptContext;
   }
   
   public boolean validate() throws PromptException
   {
      Activator.getLog().info("entra a validate");
      return true;
   }
   
   public boolean getExit(){
      return exit;
   }

   public String getRespuesta()
   {
      ElementIterator iterator = respuesta.iterator();
      StringBuffer respuestas = new StringBuffer();
      while (iterator.hasNext()){
         Element element = iterator.next();
         for ( int i=0; i < opciones.size(); i++ ){
            Opcion opcion = (Opcion) opciones.get(i);
            if ( opcion.getNumero() == element.intValue() ){
               respuestas.append(opcion.getValor());
               if (iterator.hasNext()){
                  respuestas.append("-");
               }
            }
         }
      }
      return respuestas.toString();
   }
}
