package test;

import javax.ws.rs.*;

import org.jboss.resteasy.annotations.Form;

@Path("/api")
public class LoginService
{

 
  @Path("login")
  @POST
  public String login(@FormParam("email") String e, @FormParam("password") String p) {   
   return "Logged with " + e + " " + p;
  }
  
  @Path("login2")
  @POST
  public String login( @Form LoginPayload payload) {
      System.out.println(payload.toString());
      
      return payload.toString();
  }
  
}
