/**
<Beer Converter is an Android app that converts different units and their relative alcohol content.>
    Copyright (C) <2011>  <Adam James West> ajwest@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

package ca.ajwest.BeerConverter;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


public class BeerConverter extends Activity {

	ArrayAdapter<CharSequence> adapter1, adapter2;
	Spinner spinner01, spinner02;
	EditText userInput01, userInput02, userInput03;
	double resultValue, valueAlcohol, roundedValueAlcohol, roundedResultValue;
	static int selected1, selected2;
	boolean imperialSelection = false, metricSelection = true;

	//have to initialise the string array with the value of the units in it.
	TextView resultText01, resultText02;
	String metricUnitsArray[] = {"Pint - 473 ml", "20 oz Glass - 592 ml", "Pitcher - 1774 ml", "Bottle - 341 ml", "Can - 355 ml", "Tallboy Can - 473 ml", "Mini-Pitcher - 950 ml", "Red Dixie Cup - 500 ml", "Shot - 44 ml", "Mega Mug (Schooner) - 946 ml", "King Can - 750 ml", "Jumbo King Can - 950 ml"};
	String imperialUnitsArray[] = {"Pint - 16 oz", "20 oz Glass", "Pitcher - 60 oz", "Bottle - 11.5 oz", "Can - 12 oz", "Tallboy Can - 16 oz/1 Pint", "Mini-Pitcher - 32 oz/2 Pints", "Red Dixie Cup - 16.9 oz", "Shot - 1.5 oz", "Mega Mug (Schooner) - 32 oz/2 Pints", "King Can - 25 oz", "Jumbo King Can - 32 oz/2 Pints"};
	//the lists being declared. We're going to add the units from the namesArray and valuesArray onCreate();
	List<String> valueList = new ArrayList<String>();
	List<String> nameList = new ArrayList<String>();
	List<CharSequence> customDeleteList = new ArrayList<CharSequence>();
	int defaultListLength;
	int l = 0, j = 0; //for custom unit naming and deleting



	//stuff having to do with saving preferences

	/**
	 *  The initial position of the spinner when it is first installed.
	 */
	public static final int DEFAULT_POSITION = 1;

	/**
	 * The name of a properties file that stores the position and
	 * selection when the activity is not loaded.
	 */
	public static final String PREFERENCES_FILE = "SpinnerPrefs";

	/**
	 * These values are used to read and write the properties file.
	 * PROPERTY_DELIMITER delimits the key and value in a Java properties file.
	 * The "marker" strings are used to write the properties into the file
	 */
	public static final String PROPERTY_DELIMITER = "=";

	/**
	 * The key or label for "position" in the preferences file
	 */
	public static final String POSITION_KEY1 = "Position1";
	public static final String POSITION_KEY2 = "Position2";
	public static final String POSITION_KEY3 = "Position3";
	public static final String POSITION_KEY4 = "Position4";

	/**
	 * The key or label for "selection" in the preferences file
	 */
	public static final String SELECTION_KEY = "Selection";

	public static final String POSITION_MARKER =
			POSITION_KEY1 + PROPERTY_DELIMITER;

	public static final String SELECTION_MARKER =
			SELECTION_KEY + PROPERTY_DELIMITER;

	//end stuff having to do with preferences



	@Override
	public void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		spinner01 = (Spinner) findViewById(R.id.Spinner01);
		spinner02 = (Spinner) findViewById(R.id.Spinner02);


		adapter1 = new ArrayAdapter<CharSequence> (this, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 

		//we're going to see if the user has previously selected metric or imperial and then set the spinners accordingly.
		try {
			if (readFile("unitSelect").equals("metric")){
				spinner01.setAdapter(adapter1); //for metric 
				spinner02.setAdapter(adapter1);
				imperialSelection = false;
				metricSelection = true;
			}
		} catch (IOException e1) {
			//default is to select metric because I love the metric system.
			spinner01.setAdapter(adapter1);
			spinner02.setAdapter(adapter1);
			imperialSelection = false;
			metricSelection = true;
		}

		adapter2 = new ArrayAdapter<CharSequence> (this, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 

		try {
			if (readFile("unitSelect").equals("imperial")){
				spinner01.setAdapter(adapter2); //for imperial
				spinner02.setAdapter(adapter2);
				imperialSelection = true;
				metricSelection = false;
			}
		} catch (IOException e1) {
			//default is to select metric because I love the metric system.
			spinner01.setAdapter(adapter1);
			spinner02.setAdapter(adapter1);
			imperialSelection = false;
			metricSelection = true;
		}



		//add our default units to the nameList and valueList.
		nameList.add("Pint");
		nameList.add("20 oz Glass");
		nameList.add("Pitcher");
		nameList.add("Bottle");
		nameList.add("Can");
		nameList.add("Tallboy Can");
		nameList.add("Mini-Pitcher");
		nameList.add("Red Dixie Cup");
		nameList.add("Shot");
		nameList.add("Mega Mug");
		nameList.add("King Can");
		nameList.add("Jumbo King Can");

		valueList.add("473.176473");
		valueList.add("592");
		valueList.add("1774");
		valueList.add("341");
		valueList.add("355");
		valueList.add("473");
		valueList.add("950");
		valueList.add("500");
		valueList.add("44");
		valueList.add("946.35");
		valueList.add("750");
		valueList.add("950");

		defaultListLength = nameList.size(); //so that later we know if custom units have been added.


		try {
			for (;;){ //forever loop. breaks when it can't read anymore files.
				l++;	
				String nName = "n" + l; 
				String vName = "v" + l;
				valueList.add(readFile(vName));
				nameList.add(readFile(nName));
			}
		} catch (IOException e) {
			e.printStackTrace();

			/** for programming, so that I could tell when the loop was broken.
				Context context = getApplicationContext();
				CharSequence text = "If there were custom units, they were just added to the list.";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();	
			 **/
		}


		//add all the units to the spinners
		for(int i=0;i<nameList.size();i++){
			//metric
			adapter1.add(nameList.get(i) + " - " + valueList.get(i) + " ml");

			//now imperial
			//round to nearest tenth because the value list stores the numbers in metric
			double resultV = Double.parseDouble(valueList.get(i))/29.5735296875;
			double v = (resultV * 10) + 0.5;
			double roundedResultV = ((int) v);
			roundedResultV = roundedResultV/10;

			adapter2.add(nameList.get(i) + " - " + roundedResultV + " oz");
		}


		//create the listeners
		spinner01.setOnItemSelectedListener(new MyOnItemSelectedListener1());
		spinner02.setOnItemSelectedListener(new MyOnItemSelectedListener2());

		//make the EditTexts
		userInput01 = (EditText) findViewById(R.id.inputNum1);
		userInput02 = (EditText) findViewById(R.id.inputNum2);
		userInput03 = (EditText) findViewById(R.id.inputNum3);

		resultText01 = (TextView)findViewById(R.id.SpinnerResult01);
		resultText02 = (TextView)findViewById(R.id.SpinnerResult02);

		//we want to call calculate whenever the text changes
		userInput01.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});

		userInput02.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});

		userInput03.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});


		calculate();

	}

	public void calculate() {

		if (userInput01.getText().length() != 0){
			//we should, but not the Alcohol percentages

			//so the resultValue is the inputbox * the first spinner's corresponding value / the second spinner's cooresponding value
			resultValue = Double.parseDouble(userInput01.getText().toString()) * Double.parseDouble(valueList.get(selected1)) / Double.parseDouble(valueList.get(selected2)) ;		

			//round to nearest hundredth (realistic, but really it should be rounding to the nearest sig fig of the user input
			resultValue = (resultValue * 100) + 0.5;
			roundedResultValue = ((int) resultValue);
			roundedResultValue = roundedResultValue/100;

			resultText01.setText(roundedResultValue + "");
			resultText02.setText("Explanation: " + userInput01.getText().toString() + " " + nameList.get(selected1) + "s = " + roundedResultValue + " " + nameList.get(selected2) + "s");

			if ((userInput02.getText().length() != 0) && (userInput03.getText().length() != 0)){
				//if there's stuff in all three boxes, we do all the math
				//all the top stuff divided by the product of the bottom stuff

				//so the resultValue is the inputbox * the first spinner's corresponding value / the second spinner's cooresponding value
				//(but remember, we've also multiplied this by 100 so that we can round it right after.
				resultValue = Double.parseDouble(userInput01.getText().toString()) * Double.parseDouble(valueList.get(selected1)) * Double.parseDouble(userInput02.getText().toString())  / ((Double.parseDouble(userInput03.getText().toString()))/100) / Double.parseDouble(valueList.get(selected2)) ;		
				roundedResultValue = ((int) resultValue);
				roundedResultValue = roundedResultValue/100;

				resultText01.setText(roundedResultValue + "");

				//calculate total alcohol (but remember, we've also multiplied this by 10 so that we can round it right after.
				valueAlcohol = (Double.parseDouble(userInput01.getText().toString()) * Double.parseDouble(valueList.get(selected1))) * 10 * ((Double.parseDouble(userInput02.getText().toString()))/100);

				setExplainText();
			}	
		}
	}

	//Explanation:
	String firstUnitName, secondUnitName;
	private void setExplainText() {
		
		
		String endOfExplain = ""; //have to initialise by default.
		
		if (metricSelection == true){
			roundedValueAlcohol = ((int) valueAlcohol);
			endOfExplain = " ml of ethanol.";
		}
		if (imperialSelection == true){ 
			roundedValueAlcohol = ((int) (valueAlcohol / 29.5735296875)); //convert to imperial
			endOfExplain = " oz of ethanol.";
		}
		roundedValueAlcohol = roundedValueAlcohol/10; //gotta divide that 10 out again.		

		//to make it easier to compile the full string, we're going to break down each piece.
		String firstUnitQuantity = userInput01.getText().toString();
		if (firstUnitQuantity.equals("1")){ //do we have to pluralise?
			firstUnitName = nameList.get(selected1);	
		}else{ //we pluralise
			if (nameList.get(selected1).equals("20 oz Glass")){ //check to see if it's 'Glass' so that we add 'es'
				firstUnitName = nameList.get(selected1) + "es";
			}else{ //otherwise just add 's'
				firstUnitName = nameList.get(selected1) + "s";				
			}
		}
		String firstPercentage = userInput02.getText().toString() + "%";
		String secondUnitQuantity = roundedResultValue + "";
		if (secondUnitQuantity.equals("1.0")){
			secondUnitName = nameList.get(selected2); 
		}else{
			if (nameList.get(selected2).equals("20 oz Glass")){
				secondUnitName = nameList.get(selected2) + "es";
			}else{
				secondUnitName = nameList.get(selected2) + "s";
			}
		}
		String secondPercentage = userInput03.getText().toString() + "%";
		
		resultText02.setText(firstUnitQuantity + " '" + firstUnitName + "' at " + firstPercentage + " = " + secondUnitQuantity + " '" + secondUnitName + "' at " + secondPercentage + ". \n" + "That's " + roundedValueAlcohol +  endOfExplain);
	}

	@Override
	public void onPause() {

		/*
		 * an override to onPause() must call the super constructor first.
		 */

		super.onPause();

		/*
		 * Save the state to the preferences file. If it fails, display a Toast, noting the failure.
		 */

		if (!writeInstanceState(this)) {
			Toast.makeText(this,
					"Failed to write state!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {

		/*
		 * an override to onResume() must call the super constructor first.
		 */

		super.onResume();

		/*
		 * Try to read the preferences file. If not found, set the state to the desired initial
		 * values.
		 */

		if (!readInstanceState(this)) setInitialState();

		/*
		 * Set the spinner to the current state.
		 */

		Spinner restoreSpinner1 = (Spinner)findViewById(R.id.Spinner01);
		restoreSpinner1.setSelection(selected1);
		Spinner restoreSpinner2 = (Spinner)findViewById(R.id.Spinner02);
		restoreSpinner2.setSelection(selected2);


	}

	/**
	 * Sets the initial state of the spinner when the application is first run.
	 */
	public void setInitialState() {

		BeerConverter.selected1 = DEFAULT_POSITION;
		BeerConverter.selected2 = DEFAULT_POSITION;

	}


	public class MyOnItemSelectedListener1 implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selected1 = pos;
			calculate();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListener2 implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selected2 = pos;
			calculate();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.select_metric:
			metricSelected();
			return true;
		case R.id.select_imperial:
			imperialSelected();
			return true;
		case R.id.add_unit:
			addUnitSelected();
			return true;
		case R.id.clear_units:
			clearUnitsSelected();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void clearUnitsSelected() {

		/** Deprecated. This used to delete all units.

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Confirm");

		alert.setMessage("Are you sure you want to remove all custom units?");



		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {


				for (;;){ //forever loop. breaks when it can't read anymore files.
					//We don't want to artificially add to the main number of custom units, so we're starting from 0 again with the j.
					j++;
					String nName = "n" + j; 
					String vName = "v" + j;
					if (deleteFile(nName)==true){ //we don't require a catch, and can't use a break because the deleteFile() method doesn't throw it. Instead, it returns true if it was deleted successfully.
						deleteFile(vName);	
					}else{
						//Restart the activity
						Intent intent = getIntent();
						finish();
						startActivity(intent);

						//Toast popup to confirm clearing of units.
						Context context = getApplicationContext();
						CharSequence text = "Deleted custom units.";
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
						break;
					}
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
		 **/

		//first we remove everything in our customDeleteList to update it with any changes.
		customDeleteList.clear();
		for (int i=defaultListLength; nameList.size()>i; i++){ //compares the original length of the list to the new length. So this for loop only starts if custom units were added.
			customDeleteList.add(nameList.get(i)); //for the popup later when you need to select a unit to delete.
		}
		
		if (customDeleteList.size()==0){
			Toast.makeText(getApplicationContext(), "There are no custom units to delete.", Toast.LENGTH_SHORT).show();
		}else{
		

		final CharSequence[] items = customDeleteList.toArray(new CharSequence[customDeleteList.size()]); //turns the customDeleteList into an array of CharSequence.

		//make a new Alert Dialog box that looks like a spinner. Populated with customDeleteList from above.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select unit to delete");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int pos) {
				//this is where we do stuff now that we know which custom unit the user is trying to delete.

				//we can't just delete a number out of the spinners, so we have to get the char sequence
				Object t = adapter1.getItem(pos+defaultListLength); 
				Object y = adapter2.getItem(pos+defaultListLength); //we require two, because the actual char sequence is different for metric and imperial
				adapter1.remove((CharSequence) t); //then remove it from each spinner
				adapter2.remove((CharSequence) y);

				//remove the custom unit in the nameList and valueList.
				nameList.remove(pos+defaultListLength);
				valueList.remove(pos+defaultListLength);

				Toast.makeText(getApplicationContext(), "Custom unit '" + t + "' has been deleted.", Toast.LENGTH_SHORT).show();


				// Deleting all the files and rebuilding them from the updated lists. 
				//now that the unit has been removed from the spinners (ultimately the name list and value list), we're going to delete all of the files and rebuild them. **/

				for (;;){ //forever loop. breaks when it can't read anymore files.
					//We don't want to artificially add to the main number of custom units, so we're starting from 0 again with the j.
					j++;
					String nName = "n" + j; 
					String vName = "v" + j;
					if (deleteFile(nName)==true){ //we don't require a catch, and can't use a break because the deleteFile() method doesn't throw it. Instead, it returns true if it was deleted successfully.
						deleteFile(vName);
						l--; //l needs one removed so that it knows to not add custom units past the end of the files naming convention.
						String nNameWrite = "n" + (j);
						String vNameWrite = "v" + (j);
						if (nameList.size()-defaultListLength >= j){ //since the files are being recreated from the lists, and the list have one less in them because we deleted one, we don't want to check for a value past the end of the list, or else we'll get a force close. 
							writeFile(nNameWrite, nameList.get(defaultListLength-1+j)); //subtract 1 because the defaultListLength doesn't take into account that the first item in the list is position 0.
							writeFile(vNameWrite, valueList.get(defaultListLength-1+j));
							l++; //l needs one added each time something is added to the naming convention on the list
							//								resultText01.setText(nNameWrite);  //this was for testing to see which filename was being written
							//								resultText02.setText(vNameWrite);
						}

					}else{ //when we can't delete files anymore (because there are no more), it breaks the loop.
						break;
					}
				}
			}
		});

		//even though it looks like all the stuff has already happened, that was just the listener which actually wasn't triggered until
		//after the alert dialog box was created. So here's where the initial "spinner" style dialog alert box is called.
		AlertDialog alert = builder.create();
		alert.show();
	}
	}



	String addCustomUnitName;
	public void addUnitSelected() {

		final Dialog customUnitAddDialog1 = new Dialog(this);
		customUnitAddDialog1.setContentView(R.layout.textentryalertdialog);
		customUnitAddDialog1.setTitle("Add Custom Unit...");
		TextView customUnitAddText = (TextView) customUnitAddDialog1.findViewById(R.id.messagetext);
		customUnitAddText.setText("Please input the name of your custom unit.");
		final EditText inputLine = (EditText) customUnitAddDialog1.findViewById(R.id.customUnitEditText);
		Button mAddButton1 = (Button) customUnitAddDialog1.findViewById(R.id.AddButton);
		mAddButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				customUnitAddDialog1.dismiss();
				addCustomUnitName = inputLine.getText().toString(); //We use addCustomUnitName to adapter.add in addUnitSelected2.
				//check to make sure the user actually put something in the box for a name
				if(TextUtils.isEmpty(addCustomUnitName)) {
					Context context = getApplicationContext();
					CharSequence text = "You must enter a name.";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();	
				}else{
				addUnitSelected2(); //have to call the second alert window to get the unit value.
				}
			}           
		});

		Button mCancelButton = (Button) customUnitAddDialog1.findViewById(R.id.CancelButton);
		mCancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				customUnitAddDialog1.dismiss();
			}           
		});
		customUnitAddDialog1.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		customUnitAddDialog1.show();
	}

	double impValue, metValue;
	public void addUnitSelected2(){ //this is the second method to get the unit value.

		//start attempt2

		final Dialog customUnitAddDialog2 = new Dialog(this);
		customUnitAddDialog2.setContentView(R.layout.textentryalertdialog);
		customUnitAddDialog2.setTitle("Add Custom Unit Value...");
		TextView customUnitAddText = (TextView) customUnitAddDialog2.findViewById(R.id.messagetext);

		//Going to check to see if we're on metric or imperial.
		//the variables initialise as metric for default

		if (metricSelection == true){
			customUnitAddText.setText("Input the size of one " + addCustomUnitName + " in milliliters.");
		}

		if (imperialSelection == true){
			customUnitAddText.setText("Input the size of one " + addCustomUnitName + " in ounces.");
		}

		final EditText inputLine = (EditText) customUnitAddDialog2.findViewById(R.id.customUnitEditText);
		inputLine.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); //only allows numbers with decimals
		Button mAddButton1 = (Button) customUnitAddDialog2.findViewById(R.id.AddButton);
		mAddButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				customUnitAddDialog2.dismiss();


				String value = inputLine.getText().toString();

				//got to make sure that the user gave us a valid number.

				try
				{ 
					@SuppressWarnings("unused") //just declaring a double
					double data = Double.parseDouble (value); 

					if (metricSelection == true){ //metric was selected earlier
						//so they are inputting a number in ml
						impValue = (Double.parseDouble(value) / 29.5735296875); //convert for the imperial
						metValue = (Double.parseDouble(value));
					}else{ //then imperial was selected earlier
						//so they are inputting a  number in oz
						impValue = (Double.parseDouble(value)); 
						metValue = (Double.parseDouble(value) * 29.5735296875); //convert for the metric
					}

					//lets round!
					impValue = impValue * 10;
					double roundedImpValue =  ((int) impValue);		
					roundedImpValue = roundedImpValue/10;

					metValue = metValue * 10;
					double roundedMetValue =  ((int) metValue);		
					roundedMetValue = roundedMetValue/10;

					adapter1.add(addCustomUnitName + " - " + roundedMetValue + " ml");
					adapter2.add(addCustomUnitName + " - " + roundedImpValue + " oz");

					valueList.add(roundedMetValue + ""); //add the ml value to the list
					nameList.add(addCustomUnitName); //add the unitName to nameList for the result labels

					String theCustomUnitFileNameName = "n"+l;
					String theCustomUnitFileNameValue = "v"+l;
					writeFile(theCustomUnitFileNameName, addCustomUnitName);
					writeFile(theCustomUnitFileNameValue, roundedMetValue + "");
					l++; //we want to add to the filenames for next time units are added.
				} 
				catch (NumberFormatException e) //caught when the data variable wasn't able to be set because the person didn't input a valid double
				{ 
					// You are trying to parse a double from a string that is not a double!
					Context context = getApplicationContext();
					CharSequence text = "You must enter a number.";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();	
				}


			}           
		});
		Button mCancelButton = (Button) customUnitAddDialog2.findViewById(R.id.CancelButton);
		mCancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				customUnitAddDialog2.dismiss();
			}           
		});
		customUnitAddDialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		customUnitAddDialog2.show();

		//end attempt2


	}




	public void metricSelected() {
		metricSelection = true;
		imperialSelection = false;
		spinner01.setAdapter(adapter1);
		spinner02.setAdapter(adapter1);

		//now we reselect what the user had selected before we refreshed the adapters
		spinner01.setSelection(selected1);
		spinner02.setSelection(selected2);

		//we're going to store this in a file so that we can make sure the preference is saved.
		writeFile("unitSelect", "metric");

		//and then because of the reselection we have to recalculate
		calculate();


	}

	public void imperialSelected() {
		imperialSelection = true;
		metricSelection = false;
		spinner01.setAdapter(adapter2);
		spinner02.setAdapter(adapter2);

		//now we reselect what the user had selected before we refreshed the adapters
		spinner01.setSelection(selected1);
		spinner02.setSelection(selected2);

		//we're going to store this in a file so that we can make sure the preference is saved.
		writeFile("unitSelect", "imperial");

		//and then because of the reselection we have to recalculate
		calculate();


	}


	public boolean readInstanceState(Context c) {

		/**
		 * The preferences are stored in a SharedPreferences file. The abstract implementation of
		 * SharedPreferences is a "file" containing a hashmap. All instances of an application
		 * share the same instance of this file, which means that all instances of an application
		 * share the same preference settings.
		 **/

		/*
		 * Get the SharedPreferences object for this application
		 */

		SharedPreferences p = c.getSharedPreferences(PREFERENCES_FILE, MODE_WORLD_READABLE);
		/*
		 * Get the position and value of the spinner from the file, or a default value if the
		 * key-value pair does not exist.
		 */
		selected1 = p.getInt(POSITION_KEY1, BeerConverter.DEFAULT_POSITION);
		selected2 = p.getInt(POSITION_KEY2, BeerConverter.DEFAULT_POSITION);

		/*
		 * SharedPreferences doesn't fail if the code tries to get a non-existent key. The
		 * most straightforward way to indicate success is to return the results of a test that
		 * SharedPreferences contained the position key.
		 */

		return (p.contains(POSITION_KEY1));

	}



	/*Start file reading*/
	private String readFile(String path) throws IOException {
		FileInputStream stream = openFileInput(path);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}
	/*End file reading*/

	/*Start file writing*/
	private void writeFile(String filename, String datum){

		String FILENAME = filename;
		String string = datum;

		FileOutputStream fos = null;
		try {
			fos = openFileOutput(FILENAME, Context. MODE_WORLD_READABLE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			fos.write(string.getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	/*end file writing*/
	/*start delete file*/


	public boolean deleteFile(String name){
		File dir = getFilesDir();
		File file = new File(dir, name);
		boolean deleted = file.delete();
		return deleted; //returns true if the file was deleted, false if otherwise.
	}


	/*end delete file/*

	/**
	 * Write the application's current state to a properties repository.
	 * @param c - The Activity's Context
	 *
	 */

	public boolean writeInstanceState(Context c) {

		/*
		 * Get the SharedPreferences object for this application
		 */

		SharedPreferences p =
				c.getSharedPreferences(BeerConverter.PREFERENCES_FILE, MODE_WORLD_READABLE);

		/*
		 * Get the editor for this object. The editor interface abstracts the implementation of
		 * updating the SharedPreferences object.
		 */

		SharedPreferences.Editor e = p.edit();

		/*
		 * Write the keys and values to the Editor
		 */

		e.putInt(POSITION_KEY1, BeerConverter.selected1);
		e.putInt(POSITION_KEY2, BeerConverter.selected2);	

		for (int i=defaultListLength; nameList.size()>i; i++){ //compares the original length of the list to the new length. So this for loop only starts if custom units were added.

			String filename = "n" + i;
			String valuename = "v" + i;
			writeFile(filename, nameList.get(i));
			writeFile(valuename, valueList.get(i));

		}

		/*
		 * Commit the changes. Return the result of the commit. The commit fails if Android
		 * failed to commit the changes to persistent storage.
		 */

		return (e.commit());
	}

}


