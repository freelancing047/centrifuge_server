package test;

import javax.ws.rs.FormParam;

public class LoginPayload
{
    
    private String email;
    private String password;
    
    String getEmail()
    {
        return email;
    }

    @FormParam("email")
    void setEmail( String email )
    {
        this.email = email;
    }

    String getPassword()
    {
        return password;
    }

    @FormParam("password")
    void setPassword( String password )
    {
        this.password = password;
    }


    

    @Override
    public String toString()
    {
        return "LoginPayload [email=" + email + ", password=" + password + "]";
    }
    
    

}
