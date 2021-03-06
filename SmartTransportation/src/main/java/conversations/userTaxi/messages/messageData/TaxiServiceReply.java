package conversations.userTaxi.messages.messageData;

import java.util.UUID;

import uk.ac.imperial.presage2.core.util.random.Random;

public class TaxiServiceReply 
{
	private String mMessage;
	private UUID mID;
	private UUID mTaxiID;
	public TaxiServiceReply(UUID taxiID, String message)
	{
		if(taxiID == null)
		{
			System.out.println("TaxiServiceReply() Logic error!");
		}
		assert(message != null);
		assert(taxiID != null);
		mMessage = message;
		mID = Random.randomUUID();
		mTaxiID = taxiID;
	}
	
	public UUID getID() 
	{
		return mID;
	}
	
	public String getMessage()
	{
		return mMessage;
	}
	
	public UUID getTaxiID()
	{
		return mTaxiID;
	}
}
