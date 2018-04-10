package com.juniorjl83.lexmark;

import java.util.List;

public class Servicio
{
   private String pid;
   private String nombre;
   private List encuestas;

   public Servicio()
   {
      super();
   }

   public Servicio(String nombre, List encuestas)
   {
      super();
      this.nombre = nombre;
      this.encuestas = encuestas;
   }

   public String getNombre()
   {
      return nombre;
   }

   public void setNombre(String nombre)
   {
      this.nombre = nombre;
   }

   public List getEncuestas()
   {
      return encuestas;
   }

   public void setEncuestas(List encuestas)
   {
      this.encuestas = encuestas;
   }

   public String getPid()
   {
      return pid;
   }

   public void setPid(String pid)
   {
      this.pid = pid;
   }

   public String toString()
   {
      return "Servicio [pid=" + pid + ", nombre=" + nombre + ", encuestas="
            + encuestas + "]";
   }

   
}
