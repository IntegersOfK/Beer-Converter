package ca.ajwest.BeerConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.helper.AbstractBillingActivity;
import net.robotmedia.billing.model.Transaction.PurchaseState;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class BeerConverterActivity extends AbstractBillingActivity {
	
	//Variables to create before onCreate
	String LOGS = "BeerConverterActiviy";
	ArrayAdapter<CharSequence> adapter1, adapter2;
	Boolean metricSelected, imperialSelected;
	List<String> nameList = new ArrayList<String>();
	List<String> valueList = new ArrayList<String>();
	List<CharSequence> customDeleteList = new ArrayList<CharSequence>();
	int defaultListLength;
	int l = 0, j = 0; //for custom unit naming and deleting
	static int selectedA, selectedB, selectedC, selectedD, selectedE, selectedF, selectedG, selectedH, selectedI, selectedX;
	Spinner mSpinnerA, mSpinnerB, mSpinnerC, mSpinnerD, mSpinnerE, mSpinnerF, mSpinnerG, mSpinnerH, mSpinnerI, mSpinnerX;
	static EditText mEditTextA1, mEditTextA2, mEditTextB1, mEditTextB2, mEditTextC1, mEditTextC2, mEditTextD1, mEditTextD2,mEditTextE1,
	mEditTextE2, mEditTextF1, mEditTextF2, mEditTextG1, mEditTextG2, mEditTextH1, mEditTextH2, mEditTextI1, mEditTextI2, mEditTextX1; //continued from line above
	TextView mExplainText1, mExplainText2, mTextViewHelpText1;
	LinearLayout mLinearLayoutA, mLinearLayoutB, mLinearLayoutC, mLinearLayoutD, mLinearLayoutE, mLinearLayoutF, mLinearLayoutG, mLinearLayoutH, mLinearLayoutI, mLinearLayoutX, mAddRemoveFieldsButtons;
	Button mButtonMinus, mButtonPlus;
	int extraFields = 0; //there are no extraFields at default
	public static final String PREFS_1 = "MyPrefsFile";
	int mPositionA, mPositionB, mPositionC, mPositionD, mPositionE, mPositionF, mPositionG, mPositionH, mPositionI, mPositionX;
	public boolean plusVersion; //for inapp billing
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_converter);
        
        //setting TextView for explainText
        mExplainText1 = (TextView)findViewById(R.id.ExplainText1);
        mExplainText2 = (TextView)findViewById(R.id.alcoholIndicator);
        mTextViewHelpText1 = (TextView)findViewById(R.id.textViewHelpText1);
        
        //plus and minus buttons
        mButtonMinus = (Button)findViewById(R.id.buttonMinus);
        mButtonPlus = (Button)findViewById(R.id.buttonPlus);
        
        
        //calls method to remove a field
        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	removeField(); 
            }
        });
        
        //calls method to add a field
        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	addField(); 
            }
        });
        
        
        //setting up the LinearLayouts so that we can make them VISIBLE or change their background colours for error notification
        mLinearLayoutA = (LinearLayout) findViewById(R.id.LinearLayoutA);
        mLinearLayoutB = (LinearLayout) findViewById(R.id.LinearLayoutB);
        mLinearLayoutC = (LinearLayout) findViewById(R.id.LinearLayoutC);
        mLinearLayoutD = (LinearLayout) findViewById(R.id.LinearLayoutD);
        mLinearLayoutE = (LinearLayout) findViewById(R.id.LinearLayoutE);
        mLinearLayoutF = (LinearLayout) findViewById(R.id.LinearLayoutF);
        mLinearLayoutG = (LinearLayout) findViewById(R.id.LinearLayoutG);
        mLinearLayoutH = (LinearLayout) findViewById(R.id.LinearLayoutH);
        mLinearLayoutI = (LinearLayout) findViewById(R.id.LinearLayoutI);
        mLinearLayoutX = (LinearLayout) findViewById(R.id.LinearLayoutYellow);
        mAddRemoveFieldsButtons = (LinearLayout) findViewById(R.id.AddRemoveFieldsButtons);
        
        //by default, the initial spinners are shown whereas the others aren't until purchased
        mSpinnerA = (Spinner) findViewById(R.id.SpinnerA); //SpinnerA is the first, B-I are hidden unless unlocked
        mSpinnerB = (Spinner) findViewById(R.id.SpinnerB);
        mSpinnerC = (Spinner) findViewById(R.id.SpinnerC);
        mSpinnerD = (Spinner) findViewById(R.id.SpinnerD);
        mSpinnerE = (Spinner) findViewById(R.id.SpinnerE);
        mSpinnerF = (Spinner) findViewById(R.id.SpinnerF);
        mSpinnerG = (Spinner) findViewById(R.id.SpinnerG);
        mSpinnerH = (Spinner) findViewById(R.id.SpinnerH);
        mSpinnerI = (Spinner) findViewById(R.id.SpinnerI);
		mSpinnerX = (Spinner) findViewById(R.id.SpinnerX); //SpinnerX is the 'Convert into' spinner at the end and is visible by default
        
		//Set adapters for spinners to attach to
		adapter1 = new ArrayAdapter<CharSequence> (this, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		adapter2 = new ArrayAdapter<CharSequence> (this, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 

		//we're going to see if the user has previously selected metric or imperial and then set the spinners accordingly.
		Log.i(LOGS,"You're about to get a FileNotFoundException on purpose. It's because we're looping to check for files and the exception breaks the loop.");


		
		Log.i(LOGS, "Adding default units to lists");
		//add our default units to the nameList and valueList.
		nameList.add("Pint");
		nameList.add("20 oz Glass");
		nameList.add("Pitcher");
		nameList.add("Bottle");
		nameList.add("Can");
		nameList.add("Tallboy Can");
		nameList.add("Red Dixie Cup");
		nameList.add("Shot");
		nameList.add("Mega Mug/Quart");
		nameList.add("King Can");
		nameList.add("Mini-Pitcher/Jumbo King Can");
		nameList.add("Litre");

		valueList.add("473.176473");	//Pint
		valueList.add("591.470591");	//20 oz Glass
		valueList.add("1774.41177");	//Pitcher
		valueList.add("341");			//Bottle
		valueList.add("355");			//Can
		valueList.add("473");			//Tallboy Can
		valueList.add("500");			//Red Dixie Cup
		valueList.add("44.3602943");	//Shot
		valueList.add("946.352946");	//Mega Mug/Quart
		valueList.add("750");			//King Can
		valueList.add("950");			//Mini-Pitcher/Jumbo King Can
		valueList.add("1000");			//Litre
		
		defaultListLength = nameList.size(); //so that later we know if custom units have been added.


		Log.i(LOGS, "About to loop to look for the name and value strings.");
		try {
			for (;;){ //forever loop. breaks when it can't read anymore files.
				l++;	
				String nName = "n" + l; 
				String vName = "v" + l;
				valueList.add(readFile(vName));
				nameList.add(readFile(nName));
			}
		} catch (IOException e) {
			//e.printStackTrace();
			Log.i(LOGS, "Catch caught. No more files to read. Carrying on.");
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
		

		Log.i(LOGS, "Creating listeners for the spinners and edittexts.");
		//create the listeners for all the spinners
		mSpinnerA.setOnItemSelectedListener(new MyOnItemSelectedListenerA());
		mSpinnerB.setOnItemSelectedListener(new MyOnItemSelectedListenerB());
		mSpinnerC.setOnItemSelectedListener(new MyOnItemSelectedListenerC());
		mSpinnerD.setOnItemSelectedListener(new MyOnItemSelectedListenerD());
		mSpinnerE.setOnItemSelectedListener(new MyOnItemSelectedListenerE());
		mSpinnerF.setOnItemSelectedListener(new MyOnItemSelectedListenerF());
		mSpinnerG.setOnItemSelectedListener(new MyOnItemSelectedListenerG());
		mSpinnerH.setOnItemSelectedListener(new MyOnItemSelectedListenerH());
		mSpinnerI.setOnItemSelectedListener(new MyOnItemSelectedListenerI());
		mSpinnerX.setOnItemSelectedListener(new MyOnItemSelectedListenerX());

		//create the EditTexts for everything
		mEditTextA1 =  (EditText) findViewById(R.id.EditTextA1);
		mEditTextA2 =  (EditText) findViewById(R.id.EditTextA2);
		mEditTextB1 =  (EditText) findViewById(R.id.EditTextB1);
		mEditTextB2 =  (EditText) findViewById(R.id.EditTextB2);
		mEditTextC1 =  (EditText) findViewById(R.id.EditTextC1);
		mEditTextC2 =  (EditText) findViewById(R.id.EditTextC2);
		mEditTextD1 =  (EditText) findViewById(R.id.EditTextD1);
		mEditTextD2 =  (EditText) findViewById(R.id.EditTextD2);
		mEditTextE1 =  (EditText) findViewById(R.id.EditTextE1);
		mEditTextE2 =  (EditText) findViewById(R.id.EditTextE2);
		mEditTextF1 =  (EditText) findViewById(R.id.EditTextF1);
		mEditTextF2 =  (EditText) findViewById(R.id.EditTextF2);
		mEditTextG1 =  (EditText) findViewById(R.id.EditTextG1);
		mEditTextG2 =  (EditText) findViewById(R.id.EditTextG2);
		mEditTextH1 =  (EditText) findViewById(R.id.EditTextH1);
		mEditTextH2 =  (EditText) findViewById(R.id.EditTextH2);
		mEditTextI1 =  (EditText) findViewById(R.id.EditTextI1);
		mEditTextI2 =  (EditText) findViewById(R.id.EditTextI2);
		mEditTextX1 =  (EditText) findViewById(R.id.EditTextX);
		
		
		
		/** Here we're about to implement all of the EditText Listeners so that we can call the calculate method after a user changes
		 * a value in any of the fields. Since we do the error checking in the calculate method, we don't really care what the user
		 * has input into any field, we just want to know when a value has been changed.
		 */
		
		mEditTextA1.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
			
		
		mEditTextA2.addTextChangedListener(new TextWatcher() {
			
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextB1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextB2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextC1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextC2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextD1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextD2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextE1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextE2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextF1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextF2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextG1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextG2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextH1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextH2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextI1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextI2.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		
		mEditTextX1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				//this has to be here for the TextWatcher
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			//this has to be here for the TextWatcher
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calculate();
			}});
		

		/**
		 * We have now completed the section dedicated to EditText changed listeners.
		 */

		Log.i(LOGS, "Restoring default shared prefs");
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_1, 0);
		mPositionA = settings.getInt("mPosA", 0);
		mPositionB = settings.getInt("mPosB", 0);
		mPositionC = settings.getInt("mPosC", 0);
		mPositionD = settings.getInt("mPosD", 0);
		mPositionE = settings.getInt("mPosE", 0);
		mPositionF = settings.getInt("mPosF", 0);
		mPositionG = settings.getInt("mPosG", 0);
		mPositionH = settings.getInt("mPosH", 0);
		mPositionI = settings.getInt("mPosI", 0);
		mPositionX = settings.getInt("mPosX", 0);
		String mVA1Temp = settings.getString("mVA1", "5.0");
		String mVA2Temp = settings.getString("mVA2", "1");
		String mVB1Temp = settings.getString("mVB1", "5.0");
		String mVB2Temp = settings.getString("mVB2", "1");
		String mVC1Temp = settings.getString("mVC1", "5.0");
		String mVC2Temp = settings.getString("mVC2", "1");
		String mVD1Temp = settings.getString("mVD1", "5.0");
		String mVD2Temp = settings.getString("mVD2", "1");
		String mVE1Temp = settings.getString("mVE1", "5.0");
		String mVE2Temp = settings.getString("mVE2", "1");
		String mVF1Temp = settings.getString("mVF1", "5.0");
		String mVF2Temp = settings.getString("mVF2", "1");
		String mVG1Temp = settings.getString("mVG1", "5.0");
		String mVG2Temp = settings.getString("mVG2", "1");
		String mVH1Temp = settings.getString("mVH1", "5.0");
		String mVH2Temp = settings.getString("mVH2", "1");
		String mVI1Temp = settings.getString("mVI1", "5.0");
		String mVI2Temp = settings.getString("mVI2", "1");
		String mVX1Temp = settings.getString("mVX1", "5.0");
		int extraFieldsRestore = settings.getInt("mExtraFields", 0);
		plusVersion = settings.getBoolean("plusVersionRestore", false);

		Log.i(LOGS, "Restoring spinner postions from prefs data");
		mSpinnerA.setSelection(mPositionA);
		mSpinnerB.setSelection(mPositionB);
		mSpinnerC.setSelection(mPositionC);
		mSpinnerD.setSelection(mPositionD);
		mSpinnerE.setSelection(mPositionE);
		mSpinnerF.setSelection(mPositionF);
		mSpinnerG.setSelection(mPositionG);
		mSpinnerH.setSelection(mPositionH);
		mSpinnerI.setSelection(mPositionI);
		mSpinnerX.setSelection(mPositionX);
		mEditTextA1.setText(mVA1Temp, TextView.BufferType.EDITABLE);
		mEditTextA2.setText(mVA2Temp, TextView.BufferType.EDITABLE);
		mEditTextB1.setText(mVB1Temp, TextView.BufferType.EDITABLE);
		mEditTextB2.setText(mVB2Temp, TextView.BufferType.EDITABLE);
		mEditTextC1.setText(mVC1Temp, TextView.BufferType.EDITABLE);
		mEditTextC2.setText(mVC2Temp, TextView.BufferType.EDITABLE);
		mEditTextD1.setText(mVD1Temp, TextView.BufferType.EDITABLE);
		mEditTextD2.setText(mVD2Temp, TextView.BufferType.EDITABLE);
		mEditTextE1.setText(mVE1Temp, TextView.BufferType.EDITABLE);
		mEditTextE2.setText(mVE2Temp, TextView.BufferType.EDITABLE);
		mEditTextF1.setText(mVF1Temp, TextView.BufferType.EDITABLE);
		mEditTextF2.setText(mVF2Temp, TextView.BufferType.EDITABLE);
		mEditTextG1.setText(mVG1Temp, TextView.BufferType.EDITABLE);
		mEditTextG2.setText(mVG2Temp, TextView.BufferType.EDITABLE);
		mEditTextH1.setText(mVH1Temp, TextView.BufferType.EDITABLE);
		mEditTextH2.setText(mVH2Temp, TextView.BufferType.EDITABLE);
		mEditTextI1.setText(mVI1Temp, TextView.BufferType.EDITABLE);
		mEditTextI2.setText(mVI2Temp, TextView.BufferType.EDITABLE);
		mEditTextX1.setText(mVX1Temp, TextView.BufferType.EDITABLE);
		for (int i=0; i < extraFieldsRestore; i++){
			addField();
		}
		
		
		try {
			if (readFile("unitSelect").equals("imperial")){
				imperialSelected();
			}else{
				metricSelected();
			}
		} catch (IOException e1) {
			//default is to select metric because I love the metric system.
			metricSelected();
		}
		
		
		//makes the add/remove field buttons visible if the user has purchased.
		if (plusVersion == true){ //value should be restored from shared preferences.
			mAddRemoveFieldsButtons.setVisibility(LinearLayout.VISIBLE);
		}
		
		
    }

    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_1, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putInt("mPosA", selectedA);
      editor.putInt("mPosB", selectedB);
      editor.putInt("mPosC", selectedC);
      editor.putInt("mPosD", selectedD);
      editor.putInt("mPosE", selectedE);
      editor.putInt("mPosF", selectedF);
      editor.putInt("mPosG", selectedG);
      editor.putInt("mPosH", selectedH);
      editor.putInt("mPosI", selectedI);
      editor.putInt("mPosX", selectedX);
      editor.putString("mVA1", mEditTextA1.getText().toString());
      editor.putString("mVA2", mEditTextA2.getText().toString());
      editor.putString("mVB1", mEditTextB1.getText().toString());
      editor.putString("mVB2", mEditTextB2.getText().toString());
      editor.putString("mVC1", mEditTextC1.getText().toString());
      editor.putString("mVC2", mEditTextC2.getText().toString());
      editor.putString("mVD1", mEditTextD1.getText().toString());
      editor.putString("mVD2", mEditTextD2.getText().toString());
      editor.putString("mVE1", mEditTextE1.getText().toString());
      editor.putString("mVE2", mEditTextE2.getText().toString());
      editor.putString("mVF1", mEditTextF1.getText().toString());
      editor.putString("mVF2", mEditTextF2.getText().toString());
      editor.putString("mVG1", mEditTextG1.getText().toString());
      editor.putString("mVG2", mEditTextG2.getText().toString());
      editor.putString("mVH1", mEditTextH1.getText().toString());
      editor.putString("mVH2", mEditTextH2.getText().toString());
      editor.putString("mVI1", mEditTextI1.getText().toString());
      editor.putString("mVI2", mEditTextI2.getText().toString());
      editor.putString("mVX1", mEditTextX1.getText().toString());
      editor.putInt("mExtraFields", extraFields);
      editor.putBoolean("plusVersionRestore", plusVersion);
      

      // Commit the edits!
      editor.commit();
    }
    
    
    
    /**
     * Below are subclasses required for getting the positions of each spinner.
     * You'll note that we call the calculate method after each position is determined,
     * so that we can update the explaintext and such. We do all error checking in
     * calculate method anyway, so yeah, lots of spamming that method.
     */
    
    //The listener subclasses
	public class MyOnItemSelectedListenerA implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedA = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListenerB implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedB = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListenerC implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedC = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
	
	public class MyOnItemSelectedListenerD implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedD = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
	
	public class MyOnItemSelectedListenerE implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedE = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListenerF implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedF = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListenerG implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedG = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListenerH implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedH = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class MyOnItemSelectedListenerI implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedI = pos;
			calculate(); 
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
	
	public class MyOnItemSelectedListenerX implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			selectedX = pos; 
			calculate();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
	
	
	Double spinnerValueA, spinnerValueB, spinnerValueC, spinnerValueD, spinnerValueE, spinnerValueF, spinnerValueG, spinnerValueH, spinnerValueI, spinnerValueX;
	String spinnerNameA, spinnerNameB, spinnerNameC, spinnerNameD, spinnerNameE, spinnerNameF, spinnerNameG, spinnerNameH, spinnerNameI, spinnerNameX;
	Double vA1, vA2, vB1, vB2, vC1, vC2, vD1, vD2, vE1, vE2, vF1, vF2, vG1, vG2, vH1, vH2, vI1, vI2, vX1;
	String mAString, mBString, mCString, mDString, mEString, mFString, mGString, mHString, mIString, mXString;
	private void calculate() {
		//Initialize the variables that will contain the values of the EditTexts

		//Initializing and setting spinner double values. This is done by getting them from the valueList;
		spinnerValueA = Double.parseDouble(valueList.get(selectedA));
		spinnerValueB = Double.parseDouble(valueList.get(selectedB));
		spinnerValueC = Double.parseDouble(valueList.get(selectedC));
		spinnerValueD = Double.parseDouble(valueList.get(selectedD));
		spinnerValueE = Double.parseDouble(valueList.get(selectedE));
		spinnerValueF = Double.parseDouble(valueList.get(selectedF));
		spinnerValueG = Double.parseDouble(valueList.get(selectedG));
		spinnerValueH = Double.parseDouble(valueList.get(selectedH));
		spinnerValueI = Double.parseDouble(valueList.get(selectedI));
		spinnerValueX = Double.parseDouble(valueList.get(selectedX));
		
		//getting the names of the selected spinner units. This is done by getting them from the nameList;
		spinnerNameA = nameList.get(selectedA);
		spinnerNameB = nameList.get(selectedB);
		spinnerNameC = nameList.get(selectedC);
		spinnerNameD = nameList.get(selectedD);
		spinnerNameE = nameList.get(selectedE);
		spinnerNameF = nameList.get(selectedF);
		spinnerNameG = nameList.get(selectedG);
		spinnerNameH = nameList.get(selectedH);
		spinnerNameI = nameList.get(selectedI);
		spinnerNameX = nameList.get(selectedX);


		//get the values from the EditTexts
		try{
			vA1 = Double.parseDouble(mEditTextA1.getText().toString());	
			vA2 = Double.parseDouble(mEditTextA2.getText().toString());
			mLinearLayoutA.setBackgroundResource(R.color.GREEN);
			mAString = vA2 + " " + spinnerNameA + "(" + vA1 + "%)\n";
		} catch (NumberFormatException e) {
			vA1 = 0.0;
			vA2 = 0.0;
			mLinearLayoutA.setBackgroundResource(R.color.RED);
			mAString = "\0";
		}
		if (mLinearLayoutB.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vB1 = Double.parseDouble(mEditTextB1.getText().toString());
				vB2 = Double.parseDouble(mEditTextB2.getText().toString());
				mLinearLayoutB.setBackgroundResource(R.color.GREEN);
				mBString = vB2 + " " + spinnerNameB + "(" + vB1 + "%)\n";
			} catch (NumberFormatException e) {
				vB1 = 0.0;
				vB2 = 0.0;
				mLinearLayoutB.setBackgroundResource(R.color.RED);
				mBString = "\0";
			}
		}else{
			vB1 = 0.0;
			vB2 = 0.0;
			mBString = "\0";
		}
		if (mLinearLayoutC.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vC1 = Double.parseDouble(mEditTextC1.getText().toString());
				vC2 = Double.parseDouble(mEditTextC2.getText().toString());
				mLinearLayoutC.setBackgroundResource(R.color.GREEN);
				mCString = vC2 + " " + spinnerNameC + "(" + vC1 + "%)\n";
			} catch (NumberFormatException e) {
				vC1 = 0.0;
				vC2 = 0.0;
				mLinearLayoutC.setBackgroundResource(R.color.RED);
				mCString = "\0";
			}
		}else{
			vC1 = 0.0;
			vC2 = 0.0;
			mCString = "\0";
		}
		if (mLinearLayoutD.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vD1 = Double.parseDouble(mEditTextD1.getText().toString());
				vD2 = Double.parseDouble(mEditTextD2.getText().toString());
				mLinearLayoutD.setBackgroundResource(R.color.GREEN);
				mDString = vD2 + " " + spinnerNameD + "(" + vD1 + "%)\n";
			} catch (NumberFormatException e) {
				vD1 = 0.0;
				vD2 = 0.0;
				mLinearLayoutD.setBackgroundResource(R.color.RED);
				mDString = "\0";
			}
		}else{
			vD1 = 0.0;
			vD2 = 0.0;
			mDString = "\0";
		}
		if (mLinearLayoutE.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vE1 = Double.parseDouble(mEditTextE1.getText().toString());
				vE2 = Double.parseDouble(mEditTextE2.getText().toString());
				mLinearLayoutE.setBackgroundResource(R.color.GREEN);
				mEString = vE2 + " " + spinnerNameE + "(" + vE1 + "%)\n";
			} catch (NumberFormatException e) {
				vE1 = 0.0;
				vE2 = 0.0;
				mLinearLayoutE.setBackgroundResource(R.color.RED);
				mEString = "\0";
			}
		}else{
			vE1 = 0.0;
			vE2 = 0.0;
			mEString = "\0";
		}
		if (mLinearLayoutF.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vF1 = Double.parseDouble(mEditTextF1.getText().toString());
				vF2 = Double.parseDouble(mEditTextF2.getText().toString());
				mLinearLayoutF.setBackgroundResource(R.color.GREEN);
				mFString = vF2 + " " + spinnerNameF + "(" + vF1 + "%)\n";
			} catch (NumberFormatException e) {
				vF1 = 0.0;
				vF2 = 0.0;
				mLinearLayoutF.setBackgroundResource(R.color.RED);
				mFString = "\0";
			}
		}else{
			vF1 = 0.0;
			vF2 = 0.0;
			mFString = "\0";
		}
		if (mLinearLayoutG.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vG1 = Double.parseDouble(mEditTextG1.getText().toString());
				vG2 = Double.parseDouble(mEditTextG2.getText().toString());
				mLinearLayoutG.setBackgroundResource(R.color.GREEN);
				mGString = vG2 + " " + spinnerNameG + "(" + vG1 + "%)\n";
			} catch (NumberFormatException e) {
				vG1 = 0.0;
				vG2 = 0.0;
				mLinearLayoutG.setBackgroundResource(R.color.RED);
				mGString = "\0";
			}
		}else{
			vG1 = 0.0;
			vG2 = 0.0;
			mGString = "\0";
		}
		if (mLinearLayoutH.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vH1 = Double.parseDouble(mEditTextH1.getText().toString());
				vH2 = Double.parseDouble(mEditTextH2.getText().toString());
				mLinearLayoutH.setBackgroundResource(R.color.GREEN);
				mHString = vH2 + " " + spinnerNameH + "(" + vH1 + "%)\n";
			} catch (NumberFormatException e) {
				vH1 = 0.0;
				vH2 = 0.0;
				mLinearLayoutH.setBackgroundResource(R.color.RED);
				mHString = "\0";
			}
		}else{
			vH1 = 0.0;
			vH2 = 0.0;
			mHString = "\0";
		}
		if (mLinearLayoutI.getVisibility() == View.VISIBLE){ //this if statement checks to make sure we can see the view
			try{
				vI1 = Double.parseDouble(mEditTextI1.getText().toString());
				vI2 = Double.parseDouble(mEditTextI2.getText().toString());
				mLinearLayoutI.setBackgroundResource(R.color.GREEN);
				mIString = vI2 + " " + spinnerNameI + "(" + vI1 + "%)\n";
			} catch (NumberFormatException e) {
				vI1 = 0.0;
				vI2 = 0.0;
				mLinearLayoutI.setBackgroundResource(R.color.RED);
				mIString = "\0";
			}
		}else{
			vI1 = 0.0;
			vI2 = 0.0;
			mIString = "\0";
		}
		try{
			vX1 = Double.parseDouble(mEditTextX1.getText().toString());
			mLinearLayoutX.setBackgroundResource(R.color.GREEN);
			
		} catch (NumberFormatException e) {
			vX1 = 0.0;
			mLinearLayoutX.setBackgroundResource(R.color.RED);
		}


		//Starting the actual calculations with the values that have been set above
		
		//the first section checks the default fields, as they must be filled out in order to complete the calculation.
		if (vA1 == 0.0){
			//fail, because vA1 must be filled out.
			mExplainText2.setText("Error");
			mExplainText1.setText("One or more mandatory fields are empty.");
			return;
		}
		if (vA2 == 0.0){
			//fail, because vA2 must be filled out
			mExplainText2.setText("Error");
			mExplainText1.setText("One or more mandatory fields are empty.");
			return;
		}
		if (vX1 == 0.0){
			//fail, because vX1 must be filled out
			mExplainText2.setText("Error");
			mExplainText1.setText("One or more mandatory fields are empty.");
			return;
		}
		
		//First step is going to be to add together all of the alcohol in the "convert from's"
		//divide each by 100 because it's a %
		double vTotalAlcohol = (spinnerValueA * vA1 * vA2 / 100) + (spinnerValueB * vB1 * vB2 / 100) + (spinnerValueC * vC1 * vC2 / 100) + (spinnerValueD * vD1 * vD2 / 100)
		  + (spinnerValueE * vE1 * vE2 / 100) + (spinnerValueF * vF1 * vF2 / 100) + (spinnerValueG * vG1 * vG2 / 100) + (spinnerValueH * vH1 * vH2 / 100) + (spinnerValueI * vI1 * vI2 / 100);
		
		//Now to figure out how many of the target unit that would be:
		double targetUnit =   vTotalAlcohol / (spinnerValueX * (vX1/100)) ;

		
		/**
		 * ExplainText should be formatted as this:
		 * vA2 unitA (vA1%) + vB2 unitB (vB2%) ...
		 * = resultNumber unitX at vX1%.
		 * 
		 */
		String mExplainText3;
		if (extraFields == 0){
			mExplainText3 = "The amount of alcohol in:\n " + mAString + "\n Equals: \n" + roundTwoDecimals(targetUnit) + " " + spinnerNameX + "(" + vX1 + "%)";
		}else{ //language has to change to describe the adding together process
			mExplainText3 = "You asked to add together:\n" + mAString + mBString + mCString + mDString + mEString + mFString + mGString + mHString + mIString + "\n Which equals: \n" + roundTwoDecimals(targetUnit) + " " + spinnerNameX + "(" + vX1 + "%)";
		}
		mExplainText2.setText(roundTwoDecimals(targetUnit)+"");
		mExplainText1.setText("Your total alcohol is: " + roundTwoDecimals(vTotalAlcohol) + "ml.\n" + mExplainText3);
		
		
		
		
		//TODO the next step is probably going to be getting the +/- buttons to work for the inviisble fields DONE 
		//TODO then you'll have to figure out the actual math DONE
		//TODO then you'll want to figure out how to save the states of the spinners and EditTexts in prefs, DONE and maybe make some kind of "Clear all" option LOW PRIORITY
		//it would be nice to long-hold a unit to edit it
		//TODO then you'll likely want to implement custom units DONE
		//TODO then you'll figure out inapp billing DONE
		//TODO Explain text and proper rounding
		//TODO colours
		//TODO actual testing of everything, especially inapp billing restoration and rounding.
		
	
	}
	

	private void removeField(){
		if (extraFields > 0){ //can't remove an extra field if none exist
			switch (extraFields){
			case 1:
				mLinearLayoutB.setVisibility(LinearLayout.GONE);
				mTextViewHelpText1.setText("The alcohol that you have inputted above, will be converted into the target unit below.");
				calculate();
				break;
			case 2:
				mLinearLayoutC.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			case 3:
				mLinearLayoutD.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			case 4:
				mLinearLayoutE.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			case 5:
				mLinearLayoutF.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			case 6:
				mLinearLayoutG.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			case 7:
				mLinearLayoutH.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			case 8:
				mLinearLayoutI.setVisibility(LinearLayout.GONE);
				calculate();
				break;
			default: return;
			}
		}else{
			//TODO make a toast that explains that all added fields have been removed.
			return; //return so that the extraFields -- isn't called when no field has been removed.
		}
		extraFields --;
	}
	
	private void addField() {
		if (extraFields < 8) { // there is a maximum of 8 extra fields
			mTextViewHelpText1.setText("The alcohol that you have inputted above, will be added together and converted below.");

			switch (extraFields) {
			case 0:
				mLinearLayoutB.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 1:
				mLinearLayoutC.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 2:
				mLinearLayoutD.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 3:
				mLinearLayoutE.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 4:
				mLinearLayoutF.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 5:
				mLinearLayoutG.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 6:
				mLinearLayoutH.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			case 7:
				mLinearLayoutI.setVisibility(LinearLayout.VISIBLE);
				calculate();
				break;
			default: return;
			}
		}else{
			//TODO make a toast that explains that the maximum number of fields have been added.
			return; //return so that extraFields ++ isn't called when no field has been added.
		}
			extraFields ++;
	}

    //Allows selection of  metric or imperial. Nice to have two booleans to check, so it's done this way until metric standard can be fully implemented
    private void metricSelection(boolean b) {
    	if (b == true){
    		metricSelected = true;
        	imperialSelected = false;	
    	}else{
    		metricSelected = false;
    		imperialSelected = true;
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

		final Dialog customUnitAddDialog2 = new Dialog(this);
		customUnitAddDialog2.setContentView(R.layout.textentryalertdialog);
		customUnitAddDialog2.setTitle("Add Custom Unit Value...");
		TextView customUnitAddText = (TextView) customUnitAddDialog2.findViewById(R.id.messagetext);

		//Going to check to see if we're on metric or imperial.
		//the variables initialise as metric for default

		if (metricSelected == true){
			customUnitAddText.setText("Input the size of one " + addCustomUnitName + " in milliliters.");
		}

		if (imperialSelected == true){
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

					if (metricSelected == true){ //metric was selected earlier
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

	}
	
	private void clearUnitsSelected() {
		
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

		//even though it looks like all the stuff has already happened, that was just the listener-style subclass which actually wasn't triggered until
		//after the alert dialog box was created. So here's where the initial "spinner" style dialog alert box is called.
		AlertDialog alert = builder.create();
		alert.show();
		}
	}

    
    

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
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
		case R.id.add_fields:
			addFieldsSelected();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void metricSelected() {
		mSpinnerA.setAdapter(adapter1); //for metric 
		mSpinnerB.setAdapter(adapter1);
		mSpinnerC.setAdapter(adapter1);
		mSpinnerD.setAdapter(adapter1);
		mSpinnerE.setAdapter(adapter1);
		mSpinnerF.setAdapter(adapter1);
		mSpinnerG.setAdapter(adapter1);
		mSpinnerH.setAdapter(adapter1);
		mSpinnerI.setAdapter(adapter1);
		mSpinnerX.setAdapter(adapter1);
		metricSelection(true);

		//now we reselect what the user had selected before we refreshed the adapters
		mSpinnerA.setSelection(mPositionA);
		mSpinnerB.setSelection(mPositionB);
		mSpinnerC.setSelection(mPositionC);
		mSpinnerD.setSelection(mPositionD);
		mSpinnerE.setSelection(mPositionE);
		mSpinnerF.setSelection(mPositionF);
		mSpinnerG.setSelection(mPositionG);
		mSpinnerH.setSelection(mPositionH);
		mSpinnerI.setSelection(mPositionI);
		mSpinnerX.setSelection(mPositionX);

		//we're going to store this in a file so that we can make sure the preference is saved.
		writeFile("unitSelect", "metric");

		//and then because of the reselection we have to recalculate
		calculate();

	}

	public void imperialSelected() {
		mSpinnerA.setAdapter(adapter2); //for imperial
		mSpinnerB.setAdapter(adapter2);
		mSpinnerC.setAdapter(adapter2);
		mSpinnerD.setAdapter(adapter2);
		mSpinnerE.setAdapter(adapter2);
		mSpinnerF.setAdapter(adapter2);
		mSpinnerG.setAdapter(adapter2);
		mSpinnerH.setAdapter(adapter2);
		mSpinnerI.setAdapter(adapter2);
		mSpinnerX.setAdapter(adapter2);
		metricSelection(false);

		//now we reselect what the user had selected before we refreshed the adapters
		mSpinnerA.setSelection(mPositionA);
		mSpinnerB.setSelection(mPositionB);
		mSpinnerC.setSelection(mPositionC);
		mSpinnerD.setSelection(mPositionD);
		mSpinnerE.setSelection(mPositionE);
		mSpinnerF.setSelection(mPositionF);
		mSpinnerG.setSelection(mPositionG);
		mSpinnerH.setSelection(mPositionH);
		mSpinnerI.setSelection(mPositionI);
		mSpinnerX.setSelection(mPositionX);

		//we're going to store this in a file so that we can make sure the preference is saved.
		writeFile("unitSelect", "imperial");

		//and then because of the reselection we have to recalculate
		calculate();

	}
	

	//This method allows a user to purchased added fields through in-app billing.
	public void addFieldsSelected(){
		if (plusVersion == false){
			requestPurchase("plus.version");
	//		requestPurchase("android.test.purchased"); //for testing
			return; 
		}else{
			Toast.makeText(getApplicationContext(), "Premium Version has been activated. Thanks!", Toast.LENGTH_SHORT).show();
			mAddRemoveFieldsButtons.setVisibility(LinearLayout.VISIBLE);
			//TODO Ideally we would remove the Add Fields option from the options menu. 
		}				
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
	@SuppressLint("WorldReadableFiles")
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

	public byte[] getObfuscationSalt() {
		return null;
	}

	public String getPublicKey() {
		Log.i(LOGS, "Somebody just requested the public key.");
		return <REMOVED FOR PRIVACY>;
	}

	@Override
	public void onBillingChecked(boolean supported) {
		Log.e(LOGS, "supported: " + supported);
	}

	@Override
	public void onSubscriptionChecked(boolean supported) {
		
	}

	@Override
	public void onPurchaseStateChanged(String itemId, PurchaseState state) {
		Log.i(LOGS, "onPurchaseStateChanged - itemId: " + itemId + " state: " + state);
		
		if (state.toString().equals("PURCHASED")){	
			plusVersion = true;
			addFieldsSelected();
		}
		
	}

	@Override
	public void onRequestPurchaseResponse(String itemId, ResponseCode response) {
		Log.i(LOGS, "onRequestPurchaseResponse - itemId: " + itemId + " ResponseCode: " + response);
		plusVersion = true;
		mAddRemoveFieldsButtons.setVisibility(LinearLayout.VISIBLE);

	}

	//*end delete file/*
	
	
	double roundTwoDecimals(double d)
	{
	    DecimalFormat twoDForm = new DecimalFormat("#.##");
	    return Double.valueOf(twoDForm.format(d));
	}


    
}
