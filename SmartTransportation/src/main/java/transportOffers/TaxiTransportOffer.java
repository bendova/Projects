package transportOffers;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import agents.User.TransportMode;
import agents.User.TransportPreference;
import conversations.taxiStationMediator.messageData.ITaxiDescription;
import conversations.taxiStationMediator.messageData.ITaxiServiceRequest;
import conversations.taxiStationMediator.messageData.TaxiServiceRequest;
import conversations.userMediator.messages.messageData.ITransportServiceRequest;
import conversations.userTaxi.messages.TaxiRequestCancelMessage;
import conversations.userTaxi.messages.TaxiRequestConfirmationMessage;

public class TaxiTransportOffer extends TransportOffer
{
	private ITaxiDescription mTaxiDescription;
	private ITransportServiceRequest mRequest;
	private NetworkAdaptor mNetworkAdaptor;
	private Logger mLogger;	
	
	public TaxiTransportOffer(double travelCost, double travelTime, 
			NetworkAdaptor networkAdaptor, Logger logger, 
			ITaxiDescription taxiDescription, ITransportServiceRequest request)
	{
		super(TransportMode.TAKE_TAXI);
		
		assert(travelCost >= 0);
		assert(travelTime >= 0);
		assert(networkAdaptor != null);
		assert(logger != null);
		assert(taxiDescription != null);
		assert(request != null);
		
		mTravelCost = travelCost;
		mTravelTime = travelTime;
		mNetworkAdaptor = networkAdaptor;
		mLogger = logger;
		mTaxiDescription = taxiDescription;
		mRequest = request;
	}
	
	public void setTransportServiceRequest(ITransportServiceRequest request)
	{
		assert(request != null);
		
		if(request != null)
		{
			mRequest = request;
		}
	}

	@Override
	public void confirm() 
	{
		sendConfirmationMessage();
	}

	@Override
	public void cancel() 
	{
		sendCancelMessage();
	}
	
	private void sendConfirmationMessage()
	{
		mLogger.info("sendConfirmationMessage() fromUser " + mRequest.getUserNetworkAddress());
		
		ITaxiServiceRequest request = new TaxiServiceRequest(mRequest, mTaxiDescription);
		TaxiRequestConfirmationMessage confirmationMessage = new TaxiRequestConfirmationMessage(request,
				mRequest.getUserNetworkAddress(), mTaxiDescription.getTaxiStationAddress());
		mNetworkAdaptor.sendMessage(confirmationMessage);
	}
	private void sendCancelMessage()
	{
		mLogger.info("sendCancelMessage() toUser " + mRequest.getUserNetworkAddress());
		
		TaxiRequestCancelMessage cancelMessage = new TaxiRequestCancelMessage("I cancel this request", 
				mRequest.getUserNetworkAddress(), mTaxiDescription.getTaxiStationAddress());
		mNetworkAdaptor.sendMessage(cancelMessage);
	}
	
	public ITaxiDescription getTaxiDescription()
	{
		return mTaxiDescription;
	}

	@Override
	public void applyTransportPreference(TransportPreference preference) 
	{
		scaleCost(preference.getTaxiCostScaling());		
	}
}
