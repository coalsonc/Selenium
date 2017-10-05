package Account;

//how to run task outside of eclipse
//http://doc.alertsite.com/synthetic/monitors/selenium/create-runnable-jar-from-selenium-script-using-eclipse.htm
//export runnable jar, include package required libraries into generated jar

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountCreation {
	
	//store the starting URL and the WebDriver used over and over
	public String baseUrl;
	public WebDriver driver;
	
	//whatever this is or should we even do this?
	private String credentialsFilePath = "";
	
	//whatever this is or should we even do this?
	private String projectDataFilePath = "";
	
	//required project data
	private String accountName;
	private String nameOfJiraProject;
	private String projectKey;
	private String accountDescription;
	private String projectType;
	
	//main method is required
	public static void main(String[] args) throws Exception{
		JUnitCore.main("AccountCreation.class");
	}
	
	//directing towards where Chrome driver is installed on my local machine
	//setup and teardown aren't written to test standards, so these need to be explicitly called
	//also is there a login step here? --> read from file?
	//gather (1) name of account
	//       (2) name of Jira project
	//       (3) project key for Jira project
	//       (4) description of account
	//       (5) project type
	// read from file? read from somewhere?
	
	public void Setup() {
		System.setProperty("webdriver.chrome.driver","C:/chromedriver_win32/chromedriver.exe");
		driver = new ChromeDriver(); 
		
		//point to EazyBi starting location
		baseUrl = "LOL";
		driver.get(baseUrl);
		driver.manage().window().maximize();
	}
	
	//hopefully this will work to switch over to headless
	//without changing all the commands/methods
	//no having to have Chrome or some browser in some place
	
	public void SetupHeadless() {
		driver = new HtmlUnitDriver();
		baseUrl = "LOL";
		
		driver.get(baseUrl);
		driver.manage().window().maximize();
	}
	
	//quit when finished
	public void Teardown() {
		driver.quit();
	}
	
	//maybe?
	public boolean Login(String credentialFilePath) {
		List<String> credentials = ReadFromFile(credentialFilePath);
		//click, type user-name and password, etc
		//take a screenshot of this if not successful
		//return whether it was successful or not
		
		
		TakeScreenshot("login.jpg");
		return true;
	}

	//so you want to take a screenshot, huh?
	//well tell me the filePath with the name and type for the file
	public void TakeScreenshot(String filePath) {
		//screenshot code should be something like below for reuse
		File pic = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(pic, new File(filePath));
		} catch (IOException e) {
			System.out.println("\n**Something went wrong saving the login screenshot.");
			e.printStackTrace();
		}
	}
	
	//read the project data from a file and set the class fields above for
	//reuse through this process
	public boolean CollectProjectData(String projectDataFilePath) {
		//return in order read from file?
		List<String> projectData = ReadFromFile(projectDataFilePath);
		
		//yeah, I realize this is kind of rough on the ordering
		//maybe there's a better way to do this?
		try {
			accountName = projectData.toArray()[0].toString();
			nameOfJiraProject = projectData.toArray()[1].toString();
			projectKey = projectData.toArray()[2].toString();
			accountDescription = projectData.toArray()[3].toString();
			projectType = projectData.toArray()[4].toString();
			return true;
		}
		catch(Exception e){
			
			System.out.println("\n**Something wrong with the project data read from the file.");
			System.out.println(e.getMessage());
			return false;
		}
		
	}
	
	//do the thing
	@Test
	public void StartAccountCreation() {
		//yeah, reading these from the keyboard is a temporary solution, calm down
		GetPathsFromUser();
		
		boolean loggedIn = Login(credentialsFilePath);
		boolean dataCollected = CollectProjectData(projectDataFilePath);
		
		if(loggedIn && dataCollected) {
			//start
			
			//enter account name
			WebElement accountField = FindElementById("account_name");
			accountField.sendKeys(accountName);
			
			//enter account description
			WebElement descriptionField = FindElementById("account_description");
			descriptionField.sendKeys(accountDescription);
			
			//click submit
			//need more info, more than attribute name = commit, need to know what kind of HTML tag it is in
			//maybe an enter event is good enough?
			
			//after page reload, grab the URL from this page, parse the id out of it, store it
			String id = GetIdFromUrl();
			
			//click Add new source application
			WebElement newSourceUrl = FindElementByLinkText("Add new source application");
			newSourceUrl.click();
			WaitForPageToLoad();
			
			//click the image with Jira in it
			//hopefully that can find it, is that space going to cause a problem?
			WebElement jiraImg = FindElementByCssSelector("img", "alt", "jira_local application");
			jiraImg.click();
			WaitForPageToLoad();
			
			//click submit/create
			//chose this way bc it seems unique to this element
			WebElement submitButton = FindElementByCssSelector("input", "data-disable-with", "Creating...");
			submitButton.click();
			
			//select checkbox with text [Project name][Project key]
			//id of check box is: source_application_source_selection_ids_[Project Key]
			CheckBoxById("source_application_source_selection_ids_"+ projectKey);
			
			//uncheck element with id: "source_application_import_sample_reports"
			UncheckBoxById("source_application_import_sample_reports");
			
			//check id="source_application_extra_options_import_status_transitions" name="source_application[extra_options][import_status_transitions]"
			CheckBoxById("source_application_extra_options_import_status_transitions");
			
			//check id="source_application_extra_options_import_remaining_estimated_hours_change" name="source_application[extra_options][import_remaining_estimated_hours_change]"
			CheckBoxById("source_application_extra_options_import_remaining_estimated_hours_change");
			
			//check id="source_application_extra_options_import_interval_dimensions" name="source_application[extra_options][import_interval_dimensions]"
			CheckBoxById("source_application_extra_options_import_interval_dimensions");

			//check id="source_application_extra_options_user_group_dimensions_reporter_group" id="source_application_extra_options_user_group_dimensions_reporter_group"
			//check id="source_application_extra_options_user_group_dimensions_reporter_group" name="source_application[extra_options][user_group_dimensions][]"
			//are these two duplicates?
			CheckBoxById("source_application_extra_options_user_group_dimensions_reporter_group");
			
			//check id="source_application_extra_options_user_group_dimensions_assignee_group" name="source_application[extra_options][user_group_dimensions][]"
			CheckBoxById("source_application_extra_options_user_group_dimensions_assignee_group");
		
			//check id="source_application_extra_options_user_group_dimensions_logged_by_group" name="source_application[extra_options][user_group_dimensions][]"
			CheckBoxById("source_application_extra_options_user_group_dimensions_logged_by_group");
		
			//check id="source_application_extra_options_user_group_dimensions_transition_author_group" name="source_application[extra_options][user_group_dimensions][]"
			CheckBoxById("source_application_extra_options_user_group_dimensions_transition_author_group");
			
			//click Show available custom fields
			//is this unique enough to find the right thing?
			WebElement customFields = FindElementByLinkText("Show available custom fields");
			customFields.click();
			
			//manual wait for page load
			WaitForPageToLoad();
			
			
			//click 88 stuffs in check-boxes 
			//Die();
		}
		else {
			System.out.println("\n**Something went wrong with logging in. See screenshot.");
			Teardown();
		}
	}
	
	//Check a check box if not already checked
	//Assumes we are finding the box by id
	public void CheckBoxById(String id) {
		WebElement element = FindElementById(id);
		if(!CheckOrUncheckedBox(element)) {
			//if not, check box
			element.click();
		}
	}
	
	//undo a box if not already unchecked
	//Assumes we are finding the box by id
	public void UncheckBoxById(String id) {
		WebElement element = FindElementById(id);
		if(CheckOrUncheckedBox(element)) {
			//if checked, undo this box
			element.click();
		}
	}
	
	//might be needed to get something from URL for processing
	public String GetIdFromUrl() {
		
		WaitForPageToLoad();
		String url = driver.getCurrentUrl();
		
		//parse it out somehow, based on the way it always is?
		String id = url.toString();
		
		return id;
	}
	
	//if checked, return true, else false
	public boolean CheckOrUncheckedBox(WebElement box) {
		return box.isSelected();
	}
	
	//this method can be used to find any kind of WebElement on the page
	//assuming we know the id
	public WebElement FindElementById(String id) {
		WebElement element = null;
		
		WebDriverWait wait = new WebDriverWait(driver, 100);
		element = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
		
		return element;
	}
	
	//find the element by the displayed text of the link
	public WebElement FindElementByLinkText(String text) {
		WebElement element = null;
		
		WebDriverWait wait = new WebDriverWait(driver, 100);
		element = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(text)));
		
		return element;
	}
	
	//might be unnecessary, but let's be safe when grabbing currentUrl()
	//thanks --> https://stackoverflow.com/questions/15122864/selenium-wait-until-document-is-ready
	public void WaitForPageToLoad() {
		new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
        ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
	}
	
	//this method can be used to find any kind of WebElement on the page
	//assuming we know the class
	//IMPORTANT --> singular class names only
	public WebElement FindElementByClass(String className) {
		WebElement element = null;
		
		WebDriverWait wait = new WebDriverWait(driver, 100);
		element = wait.until(ExpectedConditions.elementToBeClickable(By.className(className)));
		
		return element;
	}
	
	//so this method is going to attempt to form the CSS selector for the caller
	//mostly intended for WebElements with multiple classes
	//hopefully this works
	public WebElement FindElementByMultipleClasses(String htmlTag, String classes) {
		
		WebElement element = null;
		//so this assumes that the classes are already separated by spaces only
		String cssSelector = htmlTag + "[class='" + classes + "']";
		
		WebDriverWait wait = new WebDriverWait(driver, 100);
		element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
		
		return element;
	}
	
	//this method is going to attempt to form the cssSelector for the caller
	//works? 
	public WebElement FindElementByCssSelector(String htmlTag, String attribute, String value) {
		WebElement element = null;
		
		String cssSelector = htmlTag + "[" + attribute + "='" + value + "']";
		
		WebDriverWait wait = new WebDriverWait(driver, 100);
		element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
		return element;
	}
	
	//this might not be the end way to do this, but for now, it's probably okay
	//in the future, this will be passed in another way or gleaned in some way
	public void GetPathsFromUser() {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("\nEnter file path for credentials file: ");
        try {
			credentialsFilePath = br.readLine();
		} catch (IOException e) {
			System.out.println("\n**Error reading credentials file path input.");
			e.printStackTrace();
			Teardown();
		}
        System.out.println("\n**Enter file path for the project details: ");
        try {
			projectDataFilePath = br.readLine();
		} catch (IOException e) {
			System.out.print("\n**Error reading project details file path input.");
			e.printStackTrace();
			Teardown();
		}
	}
	
	//does the type of reader here matter, special chars from other countries?
	//thank you - https://www.caveofprogramming.com/java/java-file-reading-and-writing-files-in-java.html
	//also this might be a csv, can change logic a bit assuming the first row is a data id row
	//like Account Name, blah blah, skip that line, throw in a continue for i = 0
	public List<String> ReadFromFile(String filePath) {
		
		List<String> data = new ArrayList<String>();
		
		//can change this delimiter
		String delim = ":";
		
		String line = "";
		
		 try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(filePath);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                System.out.println(line);
	                
	                //assuming it will be like user name: user, etc
	                //hopefully no one ends their password with a space...
	                //or project names, etc
	                data.add(line.split(delim)[1].trim());
	            }   

	            // Always close files.
	            bufferedReader.close();         
	        }
	        catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                filePath + "'");                
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + filePath + "'");                  
	         
	        }
		 return data;
	    }
}
