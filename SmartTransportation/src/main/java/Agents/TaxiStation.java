package Agents;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;

import Messages.RegisterAsTaxiMessage;
import Messages.RegisterAsTaxiServiceProviderMessage;
import Messages.TaxiData;
import Messages.TaxiOrder;
import Messages.TaxiOrderMessage;
import Messages.TaxiServiceReply;
import Messages.TaxiServiceReplyMessage;
import Messages.TaxiServiceRequest;
import Messages.TaxiServiceRequestMessage;

import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;


public class TaxiStation extends AbstractParticipant
{
	private Location mLocation;
	private NetworkAddress mMediatorAddress;
	private Queue<TaxiServiceRequestMessage> mTaxiRequestMessages;
	private Set<TaxiData> mTaxiesSet;
	private Set<TaxiData> mFreeTaxiesSet;
	private ParticipantLocationService mLocationService;
	
	public TaxiStation(UUID id, String name, Location location, NetworkAddress mediatorNetworkAddress) 
	{
		super(id, name);
		mLocation = location;
		mMediatorAddress = mediatorNetworkAddress;
		
		mTaxiesSet = new HashSet<TaxiData>();
		mFreeTaxiesSet = new HashSet<TaxiData>();
		mTaxiRequestMessages = new PriorityBlockingQueue<TaxiServiceRequestMessage>();
	}
	
	public NetworkAddress getNetworkAddress() 
	{
		return network.getAddress();
	}
	
	@Override
	public void initialise()
	{
		super.initialise();
		
		registerAsTaxiServiceProvider();
		initializeLocationService();
	}
	
	private void registerAsTaxiServiceProvider()
	{
		RegisterAsTaxiServiceProviderMessage submitMessage = new 
				RegisterAsTaxiServiceProviderMessage(network.getAddress(), mMediatorAddress);
		network.sendMessage(submitMessage);
	}
	
	private void initializeLocationService()
	{
		try
		{
			mLocationService = getEnvironmentService(ParticipantLocationService.class);
		}
		catch (UnavailableServiceException e) 
		{
			logger.warn(e);
		}
	}
	
	@Override
	protected void processInput(Input input) 
	{
		logger.info("processInput() " + input);
		
		if(input != null)
		{
			if(input instanceof TaxiServiceRequestMessage)
			{
				storeRequest((TaxiServiceRequestMessage)input);
			}
			else if (input instanceof RegisterAsTaxiMessage)
			{
				registerTaxi((RegisterAsTaxiMessage)input);
			}
		}
	}
	
	private void storeRequest(TaxiServiceRequestMessage taxiRequestMessage)
	{
		logger.info("storeRequest()");
		
		mTaxiRequestMessages.add(taxiRequestMessage);
	}
	
	private void registerTaxi(RegisterAsTaxiMessage taxiRegisterMessage)
	{
		logger.info("registerTaxi() taxiRegisterMessage " + taxiRegisterMessage);
		
		TaxiData newTaxiData = taxiRegisterMessage.getData();
		if(newTaxiData != null)
		{
			mTaxiesSet.add(newTaxiData);
			mFreeTaxiesSet.add(newTaxiData);
		}
	}
	
	@Override
	public void incrementTime()
	{
		super.incrementTime();
		
		//logger.info("incrementTime() " + getTime());
		
		if((mFreeTaxiesSet.isEmpty() == false) && (mTaxiRequestMessages.isEmpty() == false))
		{
			processRequests();
		}
	}
	
	private void processRequests()
	{
		for (TaxiServiceRequestMessage taxiRequestMessage : mTaxiRequestMessages) 
		{
			if(taxiRequestMessage != null)
			{
				TaxiServiceRequest taxiServiceRequest = taxiRequestMessage.getData();
				if((taxiServiceRequest != null) && (taxiServiceRequest.isValid()))
				{
					logger.info("ProcessRequests() Processing request: " + taxiServiceRequest.GetMessage());
					
					Location userLocation = taxiServiceRequest.GetLocation();
					TaxiData nearestTaxiData = FindFreeTaxiNearestTo(userLocation);
					if(nearestTaxiData != null)
					{
						SendReplyToUser(taxiRequestMessage.getFrom(), nearestTaxiData.getID());
						SendOrderToTaxi(nearestTaxiData.getNetworkAddress(), userLocation, taxiServiceRequest.getID());
						mFreeTaxiesSet.remove(nearestTaxiData);
						mTaxiRequestMessages.remove(taxiRequestMessage);
					}
				}
			}
		}
	}
	
	private TaxiData FindFreeTaxiNearestTo(Location userLocation)
	{
		logger.info("FindFreeTaxiNearestTo() userLocation " + userLocation);
		
		TaxiData nearestTaxiData = null;
		Location nearestTaxiLocation = mLocation;
		if(mFreeTaxiesSet.isEmpty() == false)
		{
			for (TaxiData taxiData : mFreeTaxiesSet) 
			{
				Location taxiLocation = mLocationService.getAgentLocation(taxiData.getID());
				if(taxiLocation.distanceTo(userLocation) < nearestTaxiLocation.distanceTo(userLocation))
				{
					nearestTaxiLocation = taxiLocation;
					nearestTaxiData = taxiData;
				}
			}
			logger.info("FindFreeTaxiNearestTo() nearestTaxiLocation " + nearestTaxiLocation);
		}
		return nearestTaxiData;
	}
	
	private void SendReplyToUser(NetworkAddress toUser, UUID taxiID)
	{
		logger.info("SendReplyToUser() toUser " + toUser);
		
		TaxiServiceReply taxiServiceReply = new TaxiServiceReply(taxiID, "A taxi will comming your way!");
		TaxiServiceReplyMessage taxiServiceReplyMessage = 
				new TaxiServiceReplyMessage(taxiServiceReply, network.getAddress(), toUser);
		network.sendMessage(taxiServiceReplyMessage);
	}
	
	private void SendOrderToTaxi(NetworkAddress toTaxi, Location userLocation, UUID userID)
	{
		logger.info("SendOrderToTaxi() toTaxi " + toTaxi);
		
		TaxiOrder taxiOrder = new TaxiOrder(userID, userLocation);
		TaxiOrderMessage taxiOrderMessage = new TaxiOrderMessage(taxiOrder, network.getAddress(), toTaxi);
		network.sendMessage(taxiOrderMessage);
	}
}
