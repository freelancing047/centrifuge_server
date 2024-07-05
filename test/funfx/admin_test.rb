require 'test/unit'
require 'funfx'
require 'watir'

class AdminTest < Test::Unit::TestCase
  
  # define constants
  SERVERURL = "http://localhost:9090/Centrifuge/flex/index.jsp"
  LOGINTITLE = "Centrifuge Systems - Login"
  USER_ADMIN = "admin"
  USER_CENTRIFUGE = "centrifuge"
  PASSWORD = "changeme"
  
  # constants for link button names
  ABOUT = "About"
  SYSTEMADMIN = "System Administration"
  HELP = "Help"
  LOGOUT = "Log Out"
  
  # constants for tab names
  USER_MANAGE = "User Management"
  INTEGRATION = "Integration"
  ADD = "Add"
  EDIT ="Edit"
  SAVE = "Save"
  DELETE = "Delete"
  CLOSE = "Close" 
  
  OK = "OK"
  CANCEL = "Cancel"
  
  def setup
    login(USER_ADMIN, PASSWORD)
  end
  
  def test_admin
    sleep 2
    assert_not_nil(@ie.label("Welcome admin"))
    
    sleep 2
    @ie.button(ABOUT).click
    assert_not_nil(@ie.alert("About Centrifuge Server"))
    @ie.alert("About Centrifuge Server").button(OK).click

    # System Administration link
    @ie.button(SYSTEMADMIN).click
    sleep 2
    assert_not_nil(@ie.alert(SYSTEMADMIN))
    assert_not_nil(@ie.label("customer"))
    assert_equal("foo", @ie.label("customer").text)
    
    # User Management           
    @ie.tab_navigator("Admin").change(:related_object => USER_MANAGE )
    sleep 2
    @ie.data_grid("users").select(:item_renderer => USER_ADMIN)
    @ie.data_grid("users").select(:item_renderer => USER_CENTRIFUGE)
    
    # add a new user named "foo"
    @ie.button(ADD).click
    @ie.check_box("adminCheckbox").click
    edit_user("foo", "foo", "foo", "foo@centrifugesystems.com", "Admin user foo")
    
    # edit user "foo"
    @ie.data_grid("users").select(:item_renderer => "foo")
    @ie.button(EDIT).click
    @ie.check_box("adminCheckbox").click
    edit_user("foo", "foo", "foo", "foo@centrifugesystems.com", "Admin user foo modified")
    
    @ie.button(CLOSE).click
    
    # logout
    logout    
    
    # login as user "foo"    
    login( "foo", "foo") 
    
    sleep 5
    assert_not_nil(@ie.label("Welcome foo"))
    assert_nil(@ie.label("Welcome foo123"))
    logout
    
    # login as user "admin" and delete user "foo"
    login( "admin", "changeme")
    @ie.button(SYSTEMADMIN).click
    @ie.tab_navigator("Admin").change(:related_object => USER_MANAGE )
    @ie.data_grid("users").select(:item_renderer => "foo")
    @ie.button(DELETE).click
    assert_not_nil(@ie.alert("Confirm"))
    @ie.button(OK).click
    
    assert_equal(2, @ie.data_grid("users").num_rows)
    sleep 2
  end
  
  def login(user, pass)
    @ie = Funfx.instance
    @ie.start(true)
    @ie.speed = 1
    @ie.goto(SERVERURL, nil )
    sleep 5    
    @watir = Watir::IE.attach(:title, LOGINTITLE) 
    @watir.text_field(:name, "j_username").set user
    @watir.text_field(:name, "j_password").set pass
    @watir.button(:name, "submitlogin").click  
    @ie.set_flex_object("Centrifuge")
  end
  
  def logout
    @ie.button(LOGOUT).click
    assert_not_nil(@ie.alert("Confirm"))
    @ie.alert("Confirm").button(OK).click    
    assert_not_nil(@ie.title_window("Centrifuge Server Logout"))  
    @ie.unload
  end
  
  def edit_user(name, pass, confirmpass, email, remark )
    @ie.text_area("cname").input(:text => name)
    @ie.text_area("password").input(:text => pass)
    @ie.text_area("confirmPassword").input(:text => confirmpass)
    @ie.text_area("email").input(:text => email)
    @ie.text_area("remark").input(:text => remark)
    @ie.button(SAVE).click    
  end
  
  def teardown
    @ie.unload
  end
  
end