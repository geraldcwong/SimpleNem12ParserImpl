package simplenem12;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.nio.file.Files;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class SimpleNem12ParserImpl implements SimpleNem12Parser{
	
	public SimpleNem12ParserImpl()
	{}
	
	public Collection<MeterRead> parseSimpleNem12(File simpleNem12File)
	{
		Collection<MeterRead> meterReadColl = new ArrayList<MeterRead>();
		List<String> workingList = new ArrayList<String>();
		MeterRead workingMeterRead=null;
		SortedMap<LocalDate, MeterVolume> workingVolume = new TreeMap<>() ;
		MeterVolume workingMeterVolume=null;		
		String[] currLineSplit;
		int counter=0;
		String lastLine="";		
		
		try
		{
			//create a working list of records
			workingList = Files.lines(simpleNem12File.toPath()).map(s -> s.trim()) .filter(s -> !s.isEmpty()).collect(Collectors.toList());;
						
			for (int i=0;i<workingList.size();i++) {				
				currLineSplit = workingList.get(i).split(",");
				
				//Check if the file begins with 100
				if (counter==0 && !currLineSplit[0].equals("100"))
				{
					throw new Exception("File does not begin with 100");
				}
				
				if (currLineSplit[0].equals("200") || currLineSplit[0].equals("900"))
				{
					//check if there is a need to 'commit' the nmi to the meter read collection
					if (workingMeterRead!=null)
					{
						if (workingVolume.size()>0) 
						{
							workingMeterRead.setVolumes(workingVolume);							
						}
						meterReadColl.add(workingMeterRead);						
					}
					
					//start of new nmi. initialize parameters
					if (currLineSplit[0].equals("200"))
					{
						//check if nmi length is exactly 10
						if (currLineSplit[1].length()!=10)
						{
							throw new Exception("NMI should be exactly 10 digits");
						}
						workingMeterRead = new MeterRead(currLineSplit[1], EnergyUnit.valueOf(currLineSplit[2]));
						workingVolume = new TreeMap<>() ;						
					}
				}
				else if (currLineSplit[0].equals("300"))
				{					
					workingMeterVolume = new MeterVolume(new BigDecimal(currLineSplit[2]),Quality.valueOf(currLineSplit[3]));
					workingVolume.put(LocalDate.parse(currLineSplit[1], DateTimeFormatter.ofPattern("yyyyMMdd")), workingMeterVolume);
				}
				
				counter++;
				lastLine=currLineSplit[0];
				
			}
			
			//check if last line is 900
			if (!lastLine.equals("900"))
			{
				throw new Exception("File does not end with 900");
			}
		}
		catch (Exception e)
		{			
			System.out.println("An error occured." + e.getMessage());
			return null;
		}
		
		return meterReadColl;
	}

}
