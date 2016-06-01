Testiranje virtualnih senzora pomocu skripte
-----------------------------------------------------------

Ova skripta omogucuje testiranje rada virtualnih senzora 
slanjem slucajnih podataka odgovarajucem wrapperu koji drzi
otvoren port na racunalu. Wrapper te podatke oblikuje i
prosljeduje VSP-u na obradu.

Ova skripta koristi se s wrapperima SerialNetworkWrapper i
WaspWrapper.

Skripta se pokrece na sljedeci nacin:

	VStesting.py {HOST} {PORT} {MESSAGE_TYPE}
	
Argument MESSAGE_TYPE je cjelobrojna vrijednost iz intervala
[1, 4]. On označava tip poruke koji se salje wrapperu.

Wrapper WaspWrapper prima poruke oblika <ID>:<data>;<ID>:<data>...
koje zatim prosljeduju VSP-ovima kao što su TemeraturesVS,
ChartVS i ChartByIDVS. Argument MESSAGE_TYPE postavljen na 1
oznacava takvu poruku.

Wrapper SerialNetworkWrapper prima poruke oblika ID:<sensor ID>;
latitude:<lat_value>;longitude:<long_value>;<DataFieldName>:<DataFieldValue>;
<DataFieldName>:<DataFieldValue>;... Argument MESSAGE_TYPE
postavljen na vrijednosti [2, 4] oznacava takvu poruku, pri cemu
je vrijednost parametra ujedno i identifikator senzora.
Podaci se zatim prosljeduju VSP-ovima kao sto je SVGDataDisplayVS.
U tom slucaju imena koristenih polja podataka moraju biti navedena
i u XML opisniku virtualnog senzora zajedno s odgovarajucim jedinicama. 
Skripta koristi sljedeca imena polja podataka: Temperature, Humidity,
Pressure i Switch.

Same vrijednosti polja podataka su slucajne.
