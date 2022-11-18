package E_com;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Scanner;
public class Sample {
	static Scanner sc = new Scanner(System.in);
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
	
	String url = "jdbc:mysql://127.0.0.1:3306/e_com";
	String uname = "root";
	String password = "12345678";
	Class.forName("com.mysql.cj.jdbc.Driver");
	Connection con = DriverManager.getConnection(url,uname,password);
	Statement st = con.createStatement();
	      
	while(true)
		{
			System.out.println("\n~~Log In~~\n");
			System.out.println("Enter  your User ID");
			char uid = sc.nextLine().charAt(0);
			int uID =Integer.parseInt(String.valueOf(uid));
			String pass=new String(); 
			String q = selectWhere("Password","customers","Customers_ID",uID);
			    try {
			    	
			    	ResultSet rs = st.executeQuery(q);
			    	rs.next();
			    	pass = rs.getString("Password");
			    	System.out.println("Enter The password");
				    String uPass = sc.nextLine();
				    
				    if(uPass.equals(pass)) 
				    { 
				    	System.out.println("Logged in Successfully");
				    	purchaseProducts(uID,con);
				    	continue;
				    }
				    else 
				    {
				    System.out.println("Invalid Try again....\n");
				    continue;
				    }
			    }
			    catch(Exception e){
			    	System.out.println("~~Invalid ID~~");
			    	continue;
			    }
			    	
		}	
	
	}
	public static void displayProducts(Connection con,int Uid) throws SQLException {
		Statement st = con.createStatement();
		String pquery = select("*","products");
		ResultSet rs = st.executeQuery(pquery);
		System.out.println("Product ID    	 Product Name\n\n" );
    	while(rs.next()) {
    		String  pName = rs.getString("Product_name");
    		String pID = rs.getString("Product_ID");
    		System.out.println("    "+pID+"		"+pName.trim());	
    	}
    	while(true)
    	{
    		System.out.println("\nEnter Product Id to view details or To Purchase\n(0) -> Go to Home Page");
    		char pid = sc.nextLine().charAt(0);
    		int pID = Integer.parseInt(String.valueOf(pid));
    		if(pID==0) break;
    		String q = selectWhere("*","products","Product_ID",pID);
   
    		try {
		    	
		    	ResultSet rs1 = st.executeQuery(q);
		    	rs1.next();
		    	String  pName = rs1.getString("Product_name");
	    		String  pPrice = rs1.getString("MSRP");
	    		String  pDiscount = rs1.getString("Discount_rate");
	    		String  pDescription = rs1.getString("Description");
	    		String  pCount = rs1.getString("Units_in_stock"); 
	    		System.out.println("\nProduct ID : "+pID+"\nProduct Name : "+pName+"\nProduct Price : "+pPrice+"\nDiscount  : "+pDiscount+"\nProduct Description : "+pDescription+"\nTotal units left : "+pCount+"\n\n");
	    		System.out.println("(1) -> place order\n(2) -> Go back");
	    		char choice = sc.nextLine().charAt(0);
	    		int count ;
	    		switch(choice) {
	    		case '1':
	    			while(true) {
	    				System.out.println("\n(0)-> exit:\nTotal Number of units you want : ");
	    				String cnt = sc.nextLine();
		    			count= Integer.parseInt(cnt);
	    				if(count>Integer.parseInt(pCount)) {
		    				System.out.println("Out of stock... available units -> "+pCount);	
		    				continue;
		    			}
	    				break;
	    			}
	    			if (count == 0) break ;
	    			String pMethord="1";
	    			while(true) {
	    				System.out.println("Total amount to pay : "+Integer.parseInt(pPrice)*count+"Rs  Payement Methods \n1.UPI\n2.Net banking\n3.Cash on delivery \n4.EMI" );
	    				char c = sc.nextLine().charAt(0);
		    	
		    			
		    			switch(c) {
		    			case '1':
		    				pMethord = "upi";
		    				break;
		    			case '2':
		    				pMethord = "net banking";
		    				break;
		    			case '3':
		    				pMethord = "cash on delivery";
		    				break;
		    			case '4':
		    				pMethord = "emi";
		    				break;
		    			default:
		    				System.out.println("Invalid Input");	
		    			}
		    			if(pMethord !="1") break;
	    			}
	    			int a =Integer.parseInt(pCount)-count;
	    			String odr = update(pID,"products","Units_in_stock",Integer.toString(a),"Product_ID");
	    			st.executeUpdate(odr);
	    			String tt =insert("payment","Is_successful,PaymentType","'yes','"+pMethord+"'");
	    			st.executeUpdate(tt); 
	    			tt =" SELECT Payment_ID FROM e_com.payment ORDER BY Payment_ID DESC LIMIT 1;";
	    			ResultSet rs4 = st.executeQuery(tt);
	    			rs4.next();
	    			int payId = rs4.getInt("Payment_ID");
	    			tt="SELECT Supplier_ID FROM e_com.products WHERE Product_ID = "+pID+";";
	    			ResultSet rs5 = st.executeQuery(tt);
	    			rs5.next();
	    			int supId = rs5.getInt("Supplier_ID");
	    			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
    			    LocalDateTime now = LocalDateTime.now();
	    			String Odate = dtf.format(now);
	    			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    			Calendar c = Calendar.getInstance();
	    			c.add(Calendar.DATE, 2);
	    			String Ddate =  dateFormat.format(c.getTime());
	    			tt = insert("orders","Product_ID,Customer_ID,Payment_ID,Shipper_ID,Supplier_ID,OrderDate,DeliveryShipmentDate,Is_Delivered",pID+","+Uid+","+payId+","+supId+","+supId+",'"+Odate+"','"+Ddate+"',"+"'yes'");
	    			st.executeUpdate(tt);
	    			System.out.println("Payment Successful ~ Order Placed");
	    			
	    			continue;
	    		case '2':
	    			break;
	    			
	    		default :
	    			System.out.println("Invalid Input\n");
	    			continue;
	    		}
		    }
		    catch(Exception e){
		    	System.out.println("~~Invalid ID~~");
		    	continue;
		    }
    	}
    	
	}
	public static void purchaseProducts(int Uid, Connection con) throws SQLException {
		while(true) {
			System.out.println("\n(1) -> Display Product to Purchase \n(2) -> To view Past Orders and Delivery Details \n(3) -> Edit Personal Details\n(4) -> Sign out");
			char choice = sc.nextLine().charAt(0);
			int t=0;
			switch(choice){
				case '1': 
					displayProducts(con,Uid);
					break;
				case '2':
					try {
						Statement st = con.createStatement();
						String q = "SELECT p.Product_name,o.OrderDate,o.DeliveryShipmentDate  \r\n"
								+ "FROM orders AS o INNER JOIN products AS p\r\n"
								+ "ON p.Product_ID = o.Product_ID WHERE o.Customer_ID ="+Uid+";";
						ResultSet rs = st.executeQuery(q);
				    	while(rs.next()) {
				    		
				    		String pName = rs.getString("Product_name");
				    		String dDate = rs.getNString("OrderDate");
				    		String sDate = rs.getNString("DeliveryShipmentDate");    		
				    		System.out.println("\nProduct Name : "+pName+"\nDelivey Date : "+dDate+"\nOrdered date : "+sDate);
				    	}
				    	break;
					}
					catch(Exception e) {
						System.out.println(e);
					}
					break;
				case '3':
					Statement st = con.createStatement();
					String q = selectWhere("FirstName,Address_line_1,Phone","customers","Customers_ID",Uid);
					ResultSet rs = st.executeQuery(q);
					rs.next();
					String uName = rs.getString("FirstName");
					String contactNum = rs.getString("Phone");
					String address = rs.getString("Address_line_1");
					System.out.println("(1) -> Edit Name : " +uName+"\n(2) -> Edit Address : "+address+"\n(3) -> Edit Contact : "+contactNum);
					char c = sc.nextLine().charAt(0);
					String temp="1";
					switch(c) {
					case '1':
						temp="FirstName";
						break;
					case '2':
						temp="Address_line_1";
						break;
					case '3':
						temp="Phone";
						break;
					default:
						System.out.println("Invalid Input");
					}
					if(temp!="1") {
						System.out.println("Enter new "+temp+" : \n\n\r");
						String neew = sc.nextLine(); 
						String tempp=	update(Uid,"customers",temp,neew,"Customers_ID");
						st.executeUpdate(tempp);
						String q1 = selectWhere(temp,"customers","Customers_ID",Uid);
						ResultSet rs1 = st.executeQuery(q1);
						rs1.next();
						String nt = rs1.getString(temp);
						System.out.println("\n"+temp+" Updated successfully to "+nt);	
					}
			    	break;
				case '4':
					System.out.println("Signed out Successfully");
					t=1;
					break;
					
				default : 
					System.out.println("Invald Input");
					
					
			}
			if(t==1) break;
		}
	}
	public static <T> String selectWhere(String star,String table,String what,T value) {
		
		String q = new String();
		q="SELECT "+star+" FROM e_com."+table+" WHERE "+what+" = "+value +";";
		
		return q;
		
	}
	public static  String select(String star,String table) {
		
		String q = new String();
		q="SELECT "+star+" FROM e_com."+table+";";
		
		return q;
		
	}
	public static String insert(String table,String col,String values) {
		String s = "INSERT INTO e_com." +table+"("+col+") VALUES("+values+");";
		return s;
	}
	public static String update(int id,String table,String what,String set,String where) {
		String s = "UPDATE e_com."+table+" SET "+what+" = '"+set+"' WHERE "+where+" = "+id+";";
		return s;
	} 

}
