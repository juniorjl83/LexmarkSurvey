package com.juniorjl83.lexmark;

import java.util.Date;
import java.util.List;

public class Encuesta
{
   private String pid;
   private String nombre;
   private String servicio;
   private Date fechaInicio;
   private Date fechaFin;
   private List preguntas;

   public Encuesta()
   {
      super();
   }

   public Encuesta(String nombre, Date fechaInicio, Date fechaFin,
         List preguntas)
   {
      super();
      this.nombre = nombre;
      this.fechaInicio = fechaInicio;
      this.fechaFin = fechaFin;
      this.preguntas = preguntas;
   }

   public String getNombre()
   {
      return nombre;
   }

   public void setNombre(String nombre)
   {
      this.nombre = nombre;
   }

   public Date getFechaInicio()
   {
      return fechaInicio;
   }

   public void setFechaInicio(Date fechaInicio)
   {
      this.fechaInicio = fechaInicio;
   }

   public Date getFechaFin()
   {
      return fechaFin;
   }

   public void setFechaFin(Date fechaFin)
   {
      this.fechaFin = fechaFin;
   }

   public List getPreguntas()
   {
      return preguntas;
   }

   public void setPreguntas(List preguntas)
   {
      this.preguntas = preguntas;
   }

   public String getPid()
   {
      return pid;
   }

   public void setPid(String pid)
   {
      this.pid = pid;
   }
   
   public String getServicio()
   {
      return servicio;
   }

   public void setServicio(String servicio)
   {
      this.servicio = servicio;
   }

   public String toString()
   {
      return "Encuesta [nombre=" + nombre + ", fechaInicio=" + fechaInicio
            + ", fechaFin=" + fechaFin + ", preguntas=" + preguntas + "]";
   }

}
