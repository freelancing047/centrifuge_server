package csi.server.business.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.dao.CsiPersistenceManager;


public class ModelActionServiceTest {
	private ModelActionService mas;

	@Before
	public void setUp() throws Exception {
        InitializeCentrifuge.initialize();
		CsiPersistenceManager.begin();
		mas = new ModelActionService();
	}

	@After
	public void tearDown() throws Exception {
		mas = null;
		CsiPersistenceManager.close();
	}

	@Test
	public void testSave() throws Exception  {
		FieldDef fd = createTestField( "myfield", FieldType.COLUMN_REF);
		mas.save(fd);
		CsiPersistenceManager.flush();
			
		FieldDef newfd = (FieldDef) CsiPersistenceManager.findObject(FieldDef.class, fd.getUuid());
			
		assertEquals( fd.getFieldName(), newfd.getFieldName() );
		assertEquals( fd.getFieldType(), newfd.getFieldType() );
		assertEquals( fd.getDisplayFormat(), newfd.getDisplayFormat());
		assertEquals( fd.getStaticText(), newfd.getStaticText());	
	}
/*
	@Test
	public void testSaveList() throws Exception {
		FieldDef fd1 = createTestField("Last Name", FieldType.COLUMN_REF);
		FieldDef fd2 = createTestField("First Name", FieldType.COLUMN_REF);
						
		ArrayList<ModelObject> molist = new ArrayList<ModelObject>();
		molist.add( fd1 );
		molist.add(fd2);
			
		mas.saveList(molist);
			
		CsiPersistenceManager.flush();
			
		FieldDef newfd1 = (FieldDef)CsiPersistenceManager.findObject(FieldDef.class, fd1.getUuid());
		assertEquals( fd1.getFieldName(), newfd1.getFieldName());
		assertEquals( fd1.getFieldType(), newfd1.getFieldType());
			
		FieldDef newfd2 = (FieldDef)CsiPersistenceManager.findObject(FieldDef.class, fd2.getUuid());
		assertEquals( fd2.getFieldName(), newfd2.getFieldName());
		assertEquals( fd2.getFieldType(), newfd2.getFieldType());					
		
	}

	@Test
	public void testSaveAs() throws Exception {
		DataViewDef dvdef = createTestDataviewDef("testSaveAs");
		DataViewDef savedDVdef = null;
		DataViewDef newDVdef = null;
		Response<String, Resource> myResponse;

		ModelHelper.save(dvdef);
		CsiPersistenceManager.flush();
		
		myResponse = mas.saveAs(dvdef.getUuid(), dvdef.getName(), dvdef.getRemarks(), false);

		savedDVdef = (DataViewDef)myResponse.getResult();

		newDVdef = (DataViewDef) CsiPersistenceManager.findObject(DataViewDef.class,  savedDVdef.getUuid());

		assertEquals(dvdef.getName(), newDVdef.getName());
		assertEquals(dvdef.getRemarks(), newDVdef.getRemarks());
		assertEquals(dvdef.getTemplate(), newDVdef.getTemplate());

	}

	@Test
	public void testGetUniqueResourceName() throws Exception {

		DataViewDef dv = createTestDataviewDef("testGetUniqueResourceName");
		DataViewDef savedDv = null;
		DataViewDef newDv = null;
        Response<String, Resource> myResponse;

		ModelHelper.save( dv );
		CsiPersistenceManager.flush();
		
		myResponse = mas.saveAs( dv.getUuid(), dv.getName(), dv.getRemarks(), false);

		savedDv = (DataViewDef)myResponse.getResult();
		newDv = (DataViewDef)CsiPersistenceManager.findObject(DataViewDef.class,  savedDv.getUuid());

		String uniqueName = mas.getUniqueResourceName( newDv.getName());

		assertTrue(uniqueName.contains(dv.getName()));
		assertEquals(uniqueName, dv.getName()+" (1)");

	}

	@Test
	public void testIsUniqueResourceName() throws Exception {
		
		DataViewDef dvdef = createTestDataviewDef("testIsUniqueResourceName");
		DataViewDef savedDvdef = null;
			
		ModelHelper.save(dvdef);
			
		savedDvdef = (DataViewDef) ModelHelper.saveAs( Resource.class, dvdef.getUuid(), dvdef.getName(), dvdef.getRemarks(), false);
		assertFalse( mas.isUniqueResourceName(savedDvdef.getName()));
	}

	@Test
	public void testTestResourceNames() throws CentrifugeException {
		DataView dv1 = new DataView();
		DataView dv2 = new DataView();

        DataViewDef dataViewDef = new DataViewDef(true);
        dataViewDef.setModelDef(new DataModelDef());
		String dvname1 = "sample name1";
		String dvname2 = "sample name2";
						
		dv1.setName(dvname1);
		dv1.setNeedsRefresh(true);
		dv1.setSpinoff(false);
        dv1.setMeta(dataViewDef);
		dv2.setName(dvname2);
		dv2.setNeedsRefresh(false);
		dv2.setSpinoff(true);
        dv2.setMeta(dataViewDef);
			
		ArrayList<ModelObject> dvlist = new ArrayList<ModelObject>();
		dvlist.add( dv1 );
		dvlist.add( dv2 );
		mas.saveList(dvlist);
		CsiPersistenceManager.flush();
			
		ArrayList<String> names = new ArrayList<String>();
		names.add(dv1.getName());
		names.add(dv2.getName());
		ArrayList<String> exists = (ArrayList<String>)mas.testResourceNames(names);
		assertTrue( exists.contains(dvname1));
		assertTrue( exists.contains(dvname2));
	}
*/
	// utility methods to create test data
	private DataViewDef createTestDataviewDef(String name)
	{
        DataViewDef dataviewdef = new DataViewDef(ReleaseInfo.version);
		String DV_NAME = name;
		String DV_REMARKS = "Sample dataview for testing";
		String CREATE_DATE = "06/28/2010";
		String LAST_OPEN_DATE = "07/02/2010";
		String LAST_UPDATE_DATE = "07/15/2010";
		boolean isTemplate = true;
		
		Date createDate = null;
		Date lastOpenDate = null;
		Date lastUpdateDate = null;
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");		
			createDate = formatter.parse(CREATE_DATE);
			lastOpenDate = formatter.parse(LAST_OPEN_DATE);
			lastUpdateDate = formatter.parse(LAST_UPDATE_DATE);
		} catch ( ParseException pe) {
			fail( pe.getMessage());
		}
		
		dataviewdef.setName(DV_NAME);
		dataviewdef.setRemarks(DV_REMARKS);
		dataviewdef.setCreateDate(createDate);
		dataviewdef.setLastOpenDate(lastOpenDate);
		dataviewdef.setLastUpdateDate(lastUpdateDate);
		dataviewdef.setTemplate(isTemplate);
		dataviewdef.setModelDef(new DataModelDef());
		return dataviewdef;	
	}
	
	private CsiMap<String, String> createTestClientProperties() {
		
		String NAME_KEY = "Name";
		String NAME_VALUE = "Position";
		String XPOS_KEY = "xPos";
		String YPOS_KEY = "yPos";
		String ISSPINOFF_KEY = "isSpinoff";
		int XPOS_VALUE = 234;
		int YPOS_VALUE = 76;
		String ISSPINOFF = "true";

		CsiMap<String, String> props = new CsiMap<String, String>();
		props.put(NAME_KEY, NAME_VALUE );
		props.put(XPOS_KEY, Integer.toString(XPOS_VALUE ));
		props.put(YPOS_KEY, Integer.toString(YPOS_VALUE));
		props.put(ISSPINOFF_KEY, ISSPINOFF);	
		
		return props;

	}
	
	private FieldDef createTestField( String name, FieldType type ) {
		FieldDef fd = new FieldDef();
		fd.setFieldName(name);
		fd.setFieldType(type);
		fd.setDisplayFormat("MM/dd/yyyy");
		fd.setStaticText("any random text could be here!");
		return fd;
	}

}
