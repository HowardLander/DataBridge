<?php
	//session_start() must be before any output
	session_start();
?>

<!DOCTYPE html>
<!--[if lt IE 7]> <html class="no-js ie6 oldie" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7 oldie" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8 oldie" lang="en"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js" lang="en"> <!--<![endif]-->
<meta charset="utf-8">
  <head>
	<title>DataBridge data relationship visualization</title>
	<script src="https://d3js.org/d3.v3.min.js" charset="utf-8"></script>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
	<script src="d3.legend.js"></script>
	<script type="text/javascript" src="datavis.js"></script>
    <link type="text/css" rel="stylesheet" href="datavis.css"/>
	<script>
		// Read filelist.txt file to create the drop down selection form
		$(document).ready(function(){
			$.get('filelist.txt', function(data) {
				var lines = data.split("\n");
<?php
	// if there is a get request in the URL
	if (isset($_GET['data'])){
?>
				$.each(lines, function(n, elem) {
					elestr=$.trim(elem);
					if(elestr.length > 0)
						// select the file in the get request
						if(elem == "<?php echo $_GET['data']; ?>")
							$('#data-chooser').append('<option value="' + elem + '" selected>' + elem + '</option>');
						else	
							$('#data-chooser').append('<option value="' + elem + '">' + elem + '</option>');						
				});
<?php
	} else {
?>
					$.each(lines, function(n, elem) {
					elestr=$.trim(elem);
					if(elestr.length > 0)
						if(n<=0)
							$('#data-chooser').append('<option value="' + elem + '" selected>' + elem + '</option>');
						else	
							$('#data-chooser').append('<option value="' + elem + '">' + elem + '</option>');						
				});
<?php
	}	
?>
				// run the function init() in datavis.js
				init();	
			});			
		});
	</script>	
  </head>
  <body>
	<div id="container" class="container">
		<header>
			<h1>Visualization of data relationship Networks for <a href="http://databridge.web.unc.edu">DataBridge</a> </h1>
			<p id="data-desc"></p>
    	</header>
		<div id="filter" class="control">
			<label for="data-chooser"><b>Select network data: </b></label> 
			<select id="data-chooser" onchange="FileChange()">				
			</select>
			<br>			
			<label for="RNode-chooser"><b>Select Representative Nodes: </b></label> 
			<select id="RNode-chooser" onchange="RNodeChange()">
				<option value="firstnode">the first node</option>
				<option value="valuemax">the value max node</option>
				<option value="connectionmax">the connection max node(threshold = 0.5)</option>					
			</select>	
			<label for="sim-value" class="div-sep-control"><b>Filter connectivity by similarity value: </b></label>
			<select id="sim-value" onchange="SimValueChange()">
				<option value="0.2">0.2</option>
				<option value="0.3">0.3</option>
				<option value="0.4">0.4</option>
				<option value="0.5" selected>0.5</option> <!-- changed in js file to 0.6 -->
				<option value="0.6">0.6</option>
				<option value="0.7">0.7</option>
				<option value="0.8">0.8</option>
				<option value="0.9">0.9</option>
			</select>
			<button id="button" onclick="ResetView()" class="div-sep-control">Reset view</button>
			<input type="button" id="togglelayout" onclick="ToggleLayout(this)" value="Pause layout" class="div-sep-control"></input>
			<label for="search" class="div-sep-control"><b>Search: </b></label>
			<input type="text" id="search" onkeyup="updateSearch(this.value)" title="type in keywords to search data node, use comma to separate multiple search keywords."></input>
		</div>
		<div id="chart" class="canvas"></div>
		<div id="datainfo"></div>		
	</div>
  </body>
</html>
