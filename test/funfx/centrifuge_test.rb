require 'test/unit'
require 'funfx'
require 'watir'

class CentrifugeTest < Test::Unit::TestCase
  
  # define constants
  SERVERURL = "http://localhost:9090/Centrifuge/flex/index.jsp"
  LOGINTITLE = "Centrifuge Systems - Login"
  USERNAME = "admin"
  PASSWORD = "changeme"
  
  # constants for button names
  REPOSITORY = "Repository"
  REFRESH = "Refresh"
  
  SELECTALL = "Select All"
  DESELECTALL = "De-Select All"
  SPINOFF = "Spinoff"
  PUBLISH = "Publish"
  
  # chart types
  TABULAR = "Tabular"
  VBARCHART = "Vertical Bar Chart"
  HBARCHART = "Horizontal Bar Chart"
  PIECHART = "Pie Chart"
  LINECHART = "Line Chart"
  SPREADSHEET = "Spreadsheet"
  
  OK = "OK"
  # Alert titles
  
  
    
  def setup
    @ie = Funfx.instance
    @ie.start(true)
    @ie.speed = 1
    @ie.goto(SERVERURL, nil )
    sleep 5
    @watir = Watir::IE.attach(:title, LOGINTITLE) 
    @watir.text_field(:name, "j_username").set USERNAME
    @watir.text_field(:name, "j_password").set PASSWORD
    @watir.button(:name, "submitlogin").click
    @ie.set_flex_object("Centrifuge")
  end
  
  def test_centrifuge    
    table_test    
    graph_test
    chart_test   
    #repository_test
  end
  
  def table_test
    
    sleep 5
    puts "Number of Data Views: " + (@ie.list("availableViewList").num_rows).to_s
    #SAMPLE-Golf_Camp    
    
    puts @ie.list("availableViewList").children
 
    @ie.list("availableViewList").select( :item_renderer => "SAMPLE-Golf_Camp"  )
    
    sleep 2 
    @ie.list("availableViewList").select( :item_renderer => "SAMPLE-I94"  )
    sleep 2 
    @ie.list("availableViewList").select( :item_renderer => "SAMPLE-Phone"  )
    sleep 2 
    @ie.list("availableViewList").select( :item_renderer => "SAMPLE-Phone-Callback"  )
    
    sleep 2
    @ie.list("availableViewList").select( :item_renderer => "SAMPLE-Golf_Camp"  )
    @ie.list("availableViewList").double_click(:item_renderer => "SAMPLE-Golf_Camp" )
    
    #@ie.list("availableViewList").double_click(:row_index => 2 )
        
    sleep 5    
    
    first_row = "Golf,2003,1,03/08/03,03/14/03,Franklin,Louise,A,05/01/1929,519-63-3478,2838 Bernal Heights,,Rome,GA,30161,970-620-3478,970-582-8688,970-874-1145,Josef Smith MD,970-224-6473,Hay fever,,Abel Franklin,970-209-5153,,,,,150.0,06/10/2003,1350.0,,Williams, Jeff,,,,,,,";
    third_row = "Golf,2003,1,03/08/03,03/14/03,Gilmore,James,B,05/30/1977,147-62-9872,2482 Bloat Street,,Rome,GA,30161,970-499-9872,970-496-7911,970-366-8102,Anna Williams MD,970-269-2056,Penicillin,,Abrah. Gilmore,970-273-5039,,,Putting accuracy,,0.0,06/15/2003,1500.0,08/05/2003,Williams, Jeff,,,,,,,";

    numofrows = @ie.data_grid("dataGrid").num_rows
    numofcols = @ie.data_grid("dataGrid").num_columns
    puts "Number of rows in table: " + numofrows.to_s
    puts "Number of columns in table: " + numofcols.to_s
    assert_equal(first_row,  @ie.data_grid("dataGrid").tabular_data(:start => 0, :end => 0 ))
    assert_equal(third_row,  @ie.data_grid("dataGrid").tabular_data(:start => 2, :end => 2 ))
    assert_equal(80, numofrows)
    
    # Select All
    @ie.button(SELECTALL).click 
    assert_equal(0, @ie.data_grid("dataGrid").selected_index)
    sleep 2
    
    # De-Select All
    @ie.button(DESELECTALL).click
    assert_equal(-1, @ie.data_grid("dataGrid").selected_index)  
    
    # Spinoff
    @ie.button(SPINOFF).click
    assert_not_nil(@ie.alert("Warning"))
    @ie.button(OK).click
    
    @ie.button(SELECTALL).click 
    sleep 2
    @ie.button(SPINOFF).click
    assert_not_nil(@ie.alert("Create Spinoff"))
    
    spinoff_name = "Spinoff Funfx SAMPLE-Golf_Camp"
    @ie.text_area("spinoffName").input(:text => spinoff_name)
    @ie.button(OK).click
    sleep 3
    
    index = @ie.list("openViewList").selected_index
    assert(spinoff_name,  @ie.list("openViewList").tabular_data(:start => index, :end => index))
    
    # publish
    @ie.button(PUBLISH).click
    assert_not_nil(@ie.alert("Publish"))
    asset_title = "Sample Golf Camp -- Funfx"
    asset_desc = "Interesting, this is published by Funfx automation framework"
    @ie.text_area("nameText").input(:text => asset_title)
    @ie.text_area("descText").input(:text => asset_desc)
    @ie.button(OK).click
    
    sleep 3
    
  end
  
  def graph_test
    sleep 2    
    @ie.tab_navigator("tabNavigator")[1].change(:related_object => "Relationship Graph")
    # wait for relationship graph to appear
    sleep 10
    
    # Nodes list
  end
  
  def chart_test
    sleep 3
    @ie.tab_navigator("tabNavigator")[1].change(:related_object => "Chart")
    sleep 3
    
    @ie.list("dataFieldList").drag_start(:dragged_item => "Last Name")
    @ie.list("dimensionsBox").drag_drop  
    
    puts "mouse scroll down... "
    @ie.list("dataFieldList").mouse_scroll(:delta => -10)
    
    sleep 5
    @ie.list("dataFieldList").drag_start(:dragged_item => "Allergies")
    @ie.list("dimensionsBox").drag_drop   
      
    
    # Tabular chart
    sleep 2
    @ie.button(REFRESH).click
    sleep 10
    
    # Vertical Bar chart
    @ie.button(VBARCHART).click
    sleep 5
    # Horizontal Bar chart
    @ie.button(HBARCHART).click
    sleep 5
    # Pie chart
    @ie.button(PIECHART).click
    sleep 5
    # Line chart
    @ie.button(LINECHART).click
    sleep 5
    # Spreadsheet
    @ie.button(SPREADSHEET).click
    sleep 5
    
    puts "# of rows in spreadsheet view: " + @ie.data_grid("sparseDataGrid").num_rows.to_s
    puts "# of columns in spreadsheet view: " + @ie.data_grid("sparseDataGrid").num_columns.to_s   
    
    assert_equal(",Allen,Peanuts,1", @ie.data_grid("sparseDataGrid").tabular_data(:start => 0, :end => 0 ))
    assert_equal(",Williams,null,5", @ie.data_grid("sparseDataGrid").tabular_data(:start => 72, :end => 72 ))

  end
  
  def repository_test
    @ie.button(REPOSITORY).click
    assert(1, @ie.list("assetList").num_rows) # only one asset was published
    
    @ie.button("Tabular View").click
    puts "Number of published assets: " + @ie.data_grid("dataGrid")[0].num_rows.to_s
    assert_equal(1, @ie.data_grid("dataGrid")[0].num_rows)
    puts "Number of columns: " + @ie.data_grid("dataGrid")[0].num_columns.to_s
    assert_equal(6, @ie.data_grid("dataGrid")[0].num_columns)
    sleep 3
    #delete the published asset
    @ie.data_grid("dataGrid")[0].select(:row_index => 0)
    @ie.image("Delete an Asset").click
    
    assert_not_nil(@ie.alert("Confirm"))
    @ie.button(OK).click
    assert_equal(0, @ie.data_grid("dataGrid")[0].num_rows)
    
  end
  
  def teardown
    @ie.unload
  end
  
end