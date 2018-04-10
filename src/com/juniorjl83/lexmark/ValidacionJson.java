package com.juniorjl83.lexmark;

import java.util.ArrayList;
import java.util.List;

public class ValidacionJson
{
   String msgValidacion;
   List encuestas = new ArrayList();
   boolean isError = false;
   
   public String getMsgValidacion()
   {
      return msgValidacion;
   }
   public void setMsgValidacion(String msgValidacion)
   {
      this.msgValidacion = msgValidacion;
   }
   public List getEncuestas()
   {
      return encuestas;
   }
   public void setEncuestas(List encuestas)
   {
      this.encuestas = encuestas;
   }
   public boolean isError()
   {
      return isError;
   }
   public void setError(boolean isError)
   {
      this.isError = isError;
   }
   
   
}
