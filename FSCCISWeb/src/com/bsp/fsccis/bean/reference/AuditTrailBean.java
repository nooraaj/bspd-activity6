package com.bsp.fsccis.bean.reference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;

import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.bean.util.GenericLazyDataModel;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.util.ExportPdfUtil;
import com.bsp.fsccis.util.ExportXlsUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Named("auditTrailBean")
@ViewScoped
public class AuditTrailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(AuditTrailBean.class
			.getSimpleName());
	
	@Inject
	AuthBean auth;
	
	@EJB
	private GenericFacade facade;
	private List<RefAgencyGroup> agencyGroupList;
	private Map<String, RefAgencyGroup> agencyGroupMap;

	@PostConstruct
	public void init() {
		LOGGER.info("init");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		today = calendar.getTime();
		
		agencyGroupList = facade.findAll(RefAgencyGroup.class);
		getAgencyGroupMap();

		model = new GenericLazyDataModel<AuditTrail>(
				facade.getEntityManager(), AuditTrail.class) {
			private static final long serialVersionUID = 1L;


			@Override
			public void processResultObjArr() {
				LOGGER.info("processResultObjArr");
				final int AUDIT_TRAIL_ID = 0;
				
				for (Object[] obj : getResultObjArr()) {
					AuditTrail entity = facade.find(Long.valueOf(obj[AUDIT_TRAIL_ID + 1].toString()), AuditTrail.class);
					getResult().add(entity);
				}
			}
			
			@Override
			public void customInnerWhereClause(Map<String, FilterMeta> filters,
					StringBuilder sb) {
				LOGGER.info("customInnerWhereClause");
				Calendar dFrom = Calendar.getInstance();
				Calendar dTo = Calendar.getInstance();
				
				SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				
				if (dateFrom != null && dateTo == null) {
					dFrom.setTime(dateFrom);
					if(!isWhereStringEmpty()){
						sb.append(" AND ");
					}
					sb.append("timestamp(CDATE,CTIME) BETWEEN '").append(dt.format(dateFrom))
							.append("' AND '").append(dt.format(new Date())).append("' ");
					setWhereStringEmpty(false);
				}else if (dateFrom != null && dateTo != null) {
					dFrom.setTime(dateFrom);
					dTo.setTime(dateTo);
					if(!isWhereStringEmpty()){
						sb.append(" AND ");
					}
					sb.append("timestamp(CDATE,CTIME) BETWEEN '").append(dt.format(dateFrom))
							.append("' AND '").append(dt.format(dateTo)).append("' ");
					setWhereStringEmpty(false);
				}
				
				LOGGER.info(sb.toString());
			}
			
			@Override
			public void customOrderBy(Map<String, FilterMeta> filters,
					StringBuilder sb) {
				LOGGER.info("customOrderBy");
				final String loginLogoutStringFilter = AuditTrail.ACTION_LOGIN + "," + AuditTrail.ACTION_LOGOUT;
				if(filters.get("action") != null && filters.get("action").toString().equals(loginLogoutStringFilter)){
					sb.append(" ORDER BY CUSER,AUDIT_TRAIL_ID,CTIME DESC ");
				}else{
					sb.append(" ORDER BY CDATE DESC,CTIME DESC ");
				}
			}
			@Override
			public void getInnerQuery(String query){
				innerQuery = query;
			}
		};
		
		AuditTrail trail = null;
		try{
			trail = new AuditTrail("Audit Logs", AuditTrail.ACTION_VIEW);
			trail.setDetails("Audit Logs");
			trail.setAgencyGroupId(auth.getAgencyGroup());
			trail.setCdate(new Date());
			trail.setCtime(new Date());
			trail.setCuser(auth.getCuser());
			
			facade.logAuditTrail(trail);
		}catch(Exception e){
			LOGGER.info("FAILED TO LOG: " + trail.getDisplayName());
		}
	}

	private void getAgencyGroupMap() {
		agencyGroupMap = new HashMap<String, RefAgencyGroup>();
		for(RefAgencyGroup rg : agencyGroupList){
			agencyGroupMap.put("RG_"+ rg.getAgencyGroupId(), rg);
		}
		
	}

	public void pdfExport(){
		Document document = new Document();
		HttpServletResponse response = (HttpServletResponse) FacesContext
				.getCurrentInstance().getExternalContext().getResponse();

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);

			document.open();
			
			Integer ALIGN_CENTER = Element.ALIGN_CENTER;
			Integer ALIGN_LEFT = Element.ALIGN_LEFT;
			PdfPTable table = new PdfPTable(prepareExportHeader().size()); 
			ExportPdfUtil.createTableHeader(table, prepareExportHeader(), ALIGN_CENTER, ExportPdfUtil.CALIBRI_BOLD);
			ExportPdfUtil.createTableDetails(table, generateListArr(), prepareExportHeader(), ALIGN_LEFT, ExportPdfUtil.CALIBRI);
			table.setWidthPercentage(100);
			float[] floatArray = {25f, 25f, 25f, 25f, 25f, 100f};
			table.setWidths(floatArray);
			document.add(table);
			
			document.close();
			response.setHeader("Expires", "0");
			response.setHeader("Cache-Control",
					"must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Pragma", "public");
			response.setContentType("application/pdf");
			response.setContentLength(baos.size());
			response.setHeader("Content-Disposition",
					"attachment; filename=export.pdf");
			OutputStream os = response.getOutputStream();
			baos.writeTo(os);
//			os.flush();
//			os.close();
		} catch (Exception e) {
			LOGGER.info("ERROR: " + e.getMessage());
		}
		
	}

	private List<String> prepareExportHeader() {
		 List<String> h = new ArrayList<String>();
		 h.add("Agency Group");
		 h.add("Activity / Module");
		 h.add("Action");
		 h.add("User");
		 h.add("Date / Time");
		 h.add("Details");
		return h;
	}

	@SuppressWarnings("unchecked")
	private List<String[]> generateListArr() {
		DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy");
		DateFormat tformatter = new SimpleDateFormat("HH:mm");
		String[] strArr = new String[6];
		List<String[]> listStr = new ArrayList<String[]>();
		Integer m = 0;
		List<AuditTrail> a = (facade.getEntityManager().createNativeQuery(innerQuery,
				AuditTrail.class).getResultList());
		RefAgencyGroup rg = null;
		for (AuditTrail x : a) {
			strArr = new String[6];

			Integer startLoop = m;
			for (int i = 1; i <= 6; i++) {
				switch (i) {
				case 1:
					try {
						rg = agencyGroupMap.get("RG_"+ x.getAgencyGroupId().getAgencyGroupId());
						strArr[startLoop] = rg.getAgencyGroupShortname();
					} catch (Exception e) {
						strArr[startLoop] = " ";
					}
					break;
				case 2:
					strArr[startLoop] = x.getTable();
					break;
				case 3:
					strArr[startLoop] = x.getAction() == 0 ? "Add" : 
					x.getAction() == 1 ? "Edit" : 
					x.getAction() == 2 ? "View" : 
					x.getAction() == 3 ? "Login" : 
					x.getAction() == 4 ? "Logout" : 
					x.getAction() == 5 ? "Download" : 
					x.getAction() == 6 ? "Delete" : "";
					break;
				case 4:
					strArr[startLoop] =x.getCuser();
					break;
				case 5:
					strArr[startLoop] =formatter.format(x.getCdate()) + " - " + tformatter.format(x.getCtime());
					break;
				case 6:
					strArr[startLoop] = x.getDetails();
					break;
				}
				startLoop++;
			}
			listStr.add(strArr);
		}
		return listStr;
		
	}

	public void xlsExport() {
	
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet();
		ExportXlsUtil.xlsExport(wb, generateListArr(),
				prepareExportHeader());

		HttpServletResponse response = (HttpServletResponse) FacesContext
				.getCurrentInstance().getExternalContext().getResponse();
		try {
			// write it as an excel attachment
			ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
			wb.write(outByteStream);
			byte[] outArray = outByteStream.toByteArray();
			response.setContentType("application/ms-excel");
			response.setContentLength(outArray.length);
			response.setHeader("Expires:", "0");
			response.setHeader("Content-Disposition",
					"attachment; filename=export.xls");
			OutputStream outStream = response.getOutputStream();
			outStream.write(outArray);
//			outStream.flush();
		} catch (IOException e) {
			LOGGER.info("ERROR: " + e.getMessage());
		}
		
	}

	private Date today;
	private Date dateFrom;
	private Date dateTo;
	private LazyDataModel<AuditTrail> model;
	private String innerQuery;


	public Date getToday() {
		return today;
	}
	public void setToday(Date today) {
		this.today = today;
	}
	public Date getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}
	public Date getDateTo() {
		return dateTo;
	}
	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}
	public LazyDataModel<AuditTrail> getModel() {
		return model;
	}
	public void setModel(LazyDataModel<AuditTrail> model) {
		this.model = model;
	}

	public List<RefAgencyGroup> getAgencyGroupList() {
		return agencyGroupList;
	}

	public void setAgencyGroupList(List<RefAgencyGroup> agencyGroupList) {
		this.agencyGroupList = agencyGroupList;
	}

}
