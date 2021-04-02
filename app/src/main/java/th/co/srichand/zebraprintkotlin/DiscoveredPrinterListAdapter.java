/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/
package th.co.srichand.zebraprintkotlin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

import java.util.ArrayList;
import java.util.List;

public class DiscoveredPrinterListAdapter extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected Bitmap bluetoothIcon;
    protected Bitmap networkIcon;
    protected Bitmap usbIcon;
    protected List<DiscoveredPrinter> discoveredPrinters;

    public DiscoveredPrinterListAdapter(Context context) {
        super();

        mInflater = LayoutInflater.from(context);

        discoveredPrinters = new ArrayList<DiscoveredPrinter>();

        usbIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.usb);
    }

    public int getCount() {
        return discoveredPrinters.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView firstLine = null ;
        TextView secondLine = null ;

       // View retVal = mInflater.inflate(R.layout.list_item_with_image_and_two_lines, null);

        DiscoveredPrinter printer = discoveredPrinters.get(position);
        String friendlyField = "";
        if (printer instanceof DiscoveredPrinterUsb) {
        	friendlyField = "USB Printer";
        } else if (printer instanceof DiscoveredPrinterBluetooth) {
        	friendlyField = ((DiscoveredPrinterBluetooth) printer).friendlyName;
        } else if (printer instanceof DiscoveredPrinterNetwork) {
        	friendlyField = ((DiscoveredPrinterNetwork) printer).getDiscoveryDataMap().get("DNS_NAME");
        }
        firstLine.setText(friendlyField);
        secondLine.setText(printer.address);
        // return retVal;
        return null;
    }

	public void addPrinter(DiscoveredPrinter printer) {
		discoveredPrinters.add(printer);
		notifyDataSetChanged();
	}
	
	public DiscoveredPrinter getPrinter(int index) {
		return discoveredPrinters.get(index);
	}
	
	public void clearPrinters() {
		discoveredPrinters.clear();
	}
}