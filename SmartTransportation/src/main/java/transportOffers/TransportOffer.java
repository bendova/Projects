package transportOffers;

import agents.User.TransportMode;

public abstract class TransportOffer
{
	private TransportMode mTransportMode;
	protected double mTravelCost = 0.0;
	protected double mTravelTime = 0.0;
	private double mTravelCostScaleFactor = 1.0;
	public TransportOffer(TransportMode transportMode)
	{
		assert(transportMode != null);
		
		mTransportMode = transportMode;
	}
	
	public abstract void confirm();
	public abstract void cancel();
	
	public TransportMode getTransportMode()
	{
		return mTransportMode;
	}
	
	public double getCost() 
	{
		return mTravelCost;
	}
	public double getTravelTime() 
	{
		return mTravelTime;
	}
	public void scaleCost(double scale)
	{
		mTravelCost *= scale / mTravelCostScaleFactor;
		mTravelCostScaleFactor = scale;
	}
}
