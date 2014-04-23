package agents.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import agents.User.TransportPreference;

import conversations.userMediator.messages.TransportServiceOfferMessage;
import conversations.userMediator.messages.messageData.ITransportServiceOffer;
import conversations.userMediator.messages.messageData.ITransportServiceRequest;
import conversations.userMediator.messages.messageData.TransportServiceOffer;

import transportOffers.TransportOffer;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

public class TransportServiceRecord implements ITransportServiceRecord
{
	private NetworkAddress mUserAddress;
	private NetworkAddress mMediatorAddress;
	private NetworkAdaptor mNetworkAdaptor;
	private ITransportServiceRequest mTransportServiceRequest;
	private List<TransportOffer> mTransportOffers;
	private Comparator<TransportOffer> mTransportOffersComparator;
	private boolean mHasBeenUpdated;
	
	public TransportServiceRecord(NetworkAddress userAddress, ITransportServiceRequest transportServiceRequest,
			NetworkAddress mediatorAddress, NetworkAdaptor adaptor, Comparator<TransportOffer> comparator)
	{
		assert(userAddress != null);
		assert(transportServiceRequest != null);
		assert(mediatorAddress != null);
		assert(adaptor != null);
		assert(comparator != null);
		
		mUserAddress = userAddress;
		mTransportServiceRequest = transportServiceRequest;
		mMediatorAddress = mediatorAddress;
		mNetworkAdaptor = adaptor;
		mTransportOffersComparator = comparator;
		mTransportOffers = new ArrayList<TransportOffer>();
		mHasBeenUpdated = false;
	}
	@Override
	public List<TransportOffer> getTransportOffers() 
	{
		return Collections.unmodifiableList(mTransportOffers);
	}

	@Override
	public void removeTransportOffer(TransportOffer offer) 
	{
		assert(offer != null);
		assert(mTransportOffers.contains(offer) == true);
		
		mTransportOffers.remove(offer);
		mHasBeenUpdated = true;
	}
	
	@Override
	public void sendTransportOffers() 
	{
		if(mHasBeenUpdated && (mTransportOffers.isEmpty() == false))
		{
			ITransportServiceOffer offer = new TransportServiceOffer(Collections.unmodifiableList(mTransportOffers));
			TransportServiceOfferMessage msg = new TransportServiceOfferMessage(offer, mMediatorAddress, 
					mUserAddress);
			mNetworkAdaptor.sendMessage(msg);
			
			mHasBeenUpdated = false;
		}
	}
	@Override
	public ITransportServiceRequest getTransportServiceRequest() 
	{
		return mTransportServiceRequest;
	}
	
	@Override
	public void addTransportOffer(TransportOffer offer) 
	{
		assert(offer != null);
		assert(mTransportOffers.contains(offer) == false);
		
		mTransportOffers.add(offer);
		mHasBeenUpdated = true;
	}
	
	@Override
	public void sortTransportOffers()
	{
		if(mHasBeenUpdated)
		{
			applyTransportPreference(mTransportServiceRequest.getTransportPreference(), mTransportOffers);
			
			if(mTransportOffers.size() > 1)
			{
				Collections.sort(mTransportOffers, mTransportOffersComparator);
			}
		}
	}
	
	private void applyTransportPreference(TransportPreference pref, List<TransportOffer> transportOffers)
	{
		for (Iterator<TransportOffer> iterator = transportOffers.iterator(); iterator.hasNext();) 
		{
			applyTransportPreference(pref, iterator.next());
		}
	}
	
	@Override
	public void applyTransportPreference(TransportPreference pref, TransportOffer transportOffer)
	{
		double taxiCostScaling = pref.getTaxiCostScaling();
		double busCostScaling = pref.getBusCostScaling();
		double walkingCostScaling = pref.getWalkingCostScaling();
		
		switch (transportOffer.getTransportMode()) 
		{
		case TAKE_TAXI:
			transportOffer.scaleCost(taxiCostScaling);
			break;
		case TAKE_BUS:
			transportOffer.scaleCost(busCostScaling);
			break;
		case WALKING:
			transportOffer.scaleCost(walkingCostScaling);
			break;
		default:
			assert(false) : "Transport mode not handled: " + transportOffer.getTransportMode();
			break;
		}
	}
}