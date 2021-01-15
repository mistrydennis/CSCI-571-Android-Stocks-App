var express = require('express');
var app = express();
const fetch = require('node-fetch');
var cors = require('cors');
app.use(cors())
const port = 8080;
// var startDate = new Date();
var latest_price;
// app.use(express.static(process.cwd()+"/my-app/dist/my-app/"));

// app.get('*.*', (req,res) => {
// 	res.sendFile(process.cwd()+"/my-app/dist/my-app/index.html")
//   });
// API for autocomplete

app.get('/autocomplete/:keyword', function (req, res) {
    
    var url = "";
	var keyword = req.params.keyword;
	url =`https://api.tiingo.com/tiingo/utilities/search?query=${keyword}&token=1bf9a692f7987f37368d1a813bd59c94c0493fe7`;
	// console.log(url);
	fetch(url)
	.then(res => res.json())
	.then(data => {
		// console.log(data);
		res.send({ data });
		
	})
	.catch(err => {
		res.send({ status: "error"})
	})
});

// API for company's description

app.get('/company/:keyword', function (req, res) {
    
    var url = "";
	var keyword = req.params.keyword;
	url =`https://api.tiingo.com/tiingo/daily/${keyword}?token=1bf9a692f7987f37368d1a813bd59c94c0493fe7`;
	fetch(url)
	.then(res => res.json())
	.then(data => {
		res.send({ data });
	})
	.catch(err => {
		res.send({ status: "error"})
	})
});

// Company's Latest Price of stock

app.get('/company_latest_price/:keyword', latest_price =function (req, res) {
    var url = "";
	var keyword = req.params.keyword;
	url =`https://api.tiingo.com/iex/?tickers=${keyword}&token=1bf9a692f7987f37368d1a813bd59c94c0493fe7`;
	// console.log(url);
	fetch(url)
	.then(res => res.json())
	.then(data => {
		// console.log(data);
		console.log("This is the latest_price data",data[0].timestamp);
		res.send({ data });
	})
	.catch(err => {
		res.send({ status: "error"})
	})
});

//Company's historical data
app.get('/company_historical/:keyword', function (req, res) {
	ohlc=[];
	volume=[];
    var url = "";
	var keyword = req.params.keyword;
	var date = new Date();
	date.setFullYear(date.getFullYear()-2);
	date= date.getFullYear() + "-" + ("00" + (date.getMonth() + 1)).slice(-2) + "-" + ("00" + date.getDate()).slice(-2)+ " "
          + ("00" + date.getHours()).slice(-2) + ":" 
          + ("00" + date.getMinutes()).slice(-2) 
		  + ":" + ("00" + date.getSeconds()).slice(-2);
	date = date.slice(0,11);
	console.log("THis is 2 years behind",date);
	url =`https://api.tiingo.com/tiingo/daily/${keyword}/prices?startDate=${date}&columns=date,open,high,low,close,volume&token=1bf9a692f7987f37368d1a813bd59c94c0493fe7`;
	fetch(url)
	.then(res => res.json())
	.then(data => {

		var local_arr = data;
                        // console.log(local_arr);
                       
                                if(local_arr && local_arr.length>0)
                                {
                                    for(let i= 0 ;i<local_arr.length;i++)
                                    {

                                    var element = local_arr[i];
                                    this.ohlc[i] = new Array(5);
                                    
                                    var x1 = new Date(element.date);
                                    var utcDate =  Date.UTC(x1.getFullYear(),x1.getMonth(),x1.getDate());
                                    this.ohlc[i][0] = utcDate;
                                    this.ohlc[i][1] = element.open;
                                    this.ohlc[i][2] = element.high;
                                    this.ohlc[i][3] = element.low;
                                    this.ohlc[i][4] = element.close;

                                    this.volume[i] = new Array(2);
                                    this.volume[i][0] = utcDate;
									this.volume[i][1] = element.volume;
									}
								}

		res.send({ data: ohlc,volume:volume});
	})
	.catch(err => {
		res.send({ status: "error"})
	})
});

// Company's daily data
app.get('/company_daily/:keyword/:startDate',async function (req, res) {
    
    var url = "";
	var keyword = req.params.keyword;
	var startDate = new Date(req.params.startDate);
	// console.log(typeof(startDate));
	await latest_price;
	console.log(startDate);
	startDate= startDate.getFullYear() + "-" + ("00" + (startDate.getMonth() + 1)).slice(-2) + "-" + ("00" + startDate.getDate()).slice(-2)+ " "
          + ("00" + startDate.getHours()).slice(-2) + ":" 
          + ("00" + startDate.getMinutes()).slice(-2) 
		  + ":" + ("00" + startDate.getSeconds()).slice(-2);
	startDate = startDate.slice(0,11);
	url =`https://api.tiingo.com/iex/${keyword}/prices?startDate=${startDate}&resampleFreq=4min&token=1bf9a692f7987f37368d1a813bd59c94c0493fe7`;
	
	fetch(url)
	.then(res => res.json())
	.then(data => {
		console.log("Where am IDaily?");
		res.send({ data });
	})
	.catch(err => {
		res.send({ status: "error"})
	})
});


//News API
app.get('/news_data/:keyword', function (req, res) {
    
    var url = "";
	var keyword = req.params.keyword;
	url =`https://newsapi.org/v2/everything?q=${keyword}&apiKey=57dabe18bbe645a58f22308b04696501`;
	
	fetch(url)
	.then(res => res.json())
	.then(data => {
		res.send({ data });
	})
	.catch(err => {
		res.send({ status: "error"});
	})
});


app.use(function(req, res, next) {
	res.status(404).send("Route doesn't exist");
});


app.listen(port, function () {
	console.log(`Listening on port::${port}.`);
});