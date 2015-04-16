var defWidth = 960, defHeight = 600,  
	padding = 1.5, // separation between same-color nodes
	clusterPadding = 6, // separation between different-color nodes
	minRadius = 4, maxRadius = 10;
var width, height, curFileName="";
var color = d3.scale.category20();
var node_stroke_clr = d3.rgb(142, 186, 229).darker();
var node_multicluster_stroke_clr = d3.rgb(10, 10, 10);
var linkedByIndex = {};
var clusterCenterNodes = {};
var e = document.documentElement;
var g = document.getElementsByTagName('body')[0];
var bDragOn = false;
var lastSelNode = null, lastSelLink = null;
var lastSelNodeId = -1, lastSelEdgeSource = -1, lastSelEdgeTarget = -1, selNodeId = -1, selEdgeSource=-1, selEdgeTarget=-1;
var overlayRect, svg, zoom, force, link, node;
var simVal = 0.6, oriData="", filterData, tooltip;
var lastSearchStr = "", lastSearchNodes = new Array();
var bDynamicLayout = true;
	
function init() {	
	width = window.innerWidth || e.clientWidth || g.clientWidth;
	height = window.innerHeight || e.clientHeight || g.clientHeight;
	if(width <= 0) 
		width = defWidth;	
	else
		width -= 100;	
	if(height <= 0) 
		height = defHeight; 
	else
		height -= 300; // account for height of upper and lower gui elements
	document.getElementById("datainfo").style.width=width+"px";		
	
	tooltip = d3.select("body").append("div")   
		.attr("class", "tooltip")               
		.style("visibility", "hidden");
	document.getElementById('sim-value').value = simVal;    
	document.getElementById("togglelayout").value = "Pause layout";
	zoom = d3.behavior.zoom(); 	
	svg = d3.select("#chart").append("svg")
		.attr("width", width)
		.attr("height", height)
		.append("g")
		.call(zoom.scaleExtent([1, 4]).on("zoom", zoom_redraw))
		.append("g");
	overlayRect = svg.append("rect")
		.attr("class", "overlay")
		.attr("width", width)
		.attr("height", height);

	force = d3.layout.force()
		//.linkStrength(0.1)
		.friction(0.9)
		//.linkDistance(20)
		//.charge(-30)
		//.gravity(0.1)
		.theta(0.8)
		.alpha(0.1)
		.gravity(.06)
		.charge(-120)
		.linkDistance(60)
		.size([width, height]);
	window.onresize = updateWindow;
	FileChange();
	contextMenuShowing = false;
	d3.select("body").on('contextmenu', function(d, i) {
		if (contextMenuShowing) {
			d3.event.preventDefault();
			d3.select(".popup").remove();
			contextMenuShowing = false;
		} else {
			d3_target = d3.select(d3.event.target);
			if (d3_target.classed("circle")) {
				d3.event.preventDefault();
				contextMenuShowing = true;
				d = d3_target.datum();
				canvas = d3.select(".canvas");
				mousePosition = d3.mouse(canvas.node());
				popup = canvas.append("div")
					.attr("class", "popup")
					.style("left", mousePosition[0] + "px")
					.style("top", mousePosition[1] + "px");
					
				//popup.append("h2").text(d.title);
				popup.append("p")
					.append("a")
					.attr("href",d.URL)					
					.text(d.title);
				canvasSize = [
					canvas.node().offsetWidth,
					canvas.node().offsetHeight
				];
				
				popupSize = [ 
					popup.node().offsetWidth,
					popup.node().offsetHeight
				];
				
				if (popupSize[0] + mousePosition[0] > canvasSize[0]) {
					popup.style("left","auto");
					popup.style("right",0);
				}
				
				if (popupSize[1] + mousePosition[1] > canvasSize[1]) {
					popup.style("top","auto");
					popup.style("bottom",0);
				}	
			}
		}
	});
}
	
function updateWindow(){
    width = window.innerWidth || e.clientWidth || g.clientWidth;
    height = window.innerHeight|| e.clientHeight|| g.clientHeight;
    if(width <= 0) 
		width = defWidth;
	else
		width -= 100;		
	if(height <= 0) 
	 	height = defHeight; 
	else
		height -= 300; // account for height of upper and lower gui elements
    overlayRect.attr("width", width).attr("height", height);
    svg.attr("width", width).attr("height", height);
    document.getElementById("datainfo").style.width=width+"px";	
    force.size([width, height]);
}
		
function zoom_redraw() {
	 svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    //svg.attr("transform", "scale(" + d3.event.scale + ")");
} 

var node_drag = d3.behavior.drag()
	.on("dragstart", dragstart)
	.on("drag", dragmove)
	.on("dragend", dragend);	
	
function dragstart(d, i) {
	d3.event.sourceEvent.stopPropagation(); // very important; otherwise, panning will interfare with node dragging
	force.stop(); // stops the force auto positioning before you start dragging
	bDragOn = true;
}

function dragmove(d, i) {
	d.px += d3.event.dx;
	d.py += d3.event.dy;
	d.x += d3.event.dx;
	d.y += d3.event.dy; 
	tick(); // this is the key to make it work together with updating both px,py,x,y on d !
}

function dragend(d, i) {
	d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
	tick();
	bDragOn = false;
	if(bDynamicLayout)
		force.resume();
	
}

function isConnected(a, b) {
	return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index;
}

function tick(e) {
	node
		.attr("transform", function(d) { 
			d.x = Math.max(maxRadius, Math.min(width - maxRadius, d.x));
			d.y = Math.max(maxRadius, Math.min(height - maxRadius, d.y));
			return "translate(" + d.x + "," + d.y + ")"; 
		});
	
	if(!bDragOn) {
		node
			.each(cluster(10 * e.alpha * e.alpha))
			.each(collide(.5))
			.attr("cx", function(d) { return d.x; })
			.attr("cy", function(d) { return d.y; });
	}
	
	// add the curvy lines
	link.attr("d", function(d) {
		var dx = d.target.x-d.source.x, dy = d.target.y-d.source.y, dr = Math.sqrt(dx * dx + dy * dy);
		return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
	});
};

// Move d to be adjacent to the cluster node.
function cluster(alpha) {	
	return function(d) {
		var node = clusterCenterNodes[d.clr], l, r, x, y;
		if (node == d) return;
		x = d.x - node.x;
		y = d.y - node.y;
		l = Math.sqrt(x * x + y * y);
		//r = d.radius + node.radius;
		r = maxRadius + maxRadius;
		if (l != r) {
			l = (l - r) / l * alpha;
			d.x -= x *= l;
			d.y -= y *= l;
			node.x += x;
			node.y += y;
		}
	}
}

// Resolves collisions between d and all other circles.
function collide(alpha) {
    if(typeof oriData.nodes === "undefined")
        return true; 
    var quadtree = d3.geom.quadtree(oriData.nodes);
    if(typeof quadtree === "undefined")
        return true;
	return function(d) {
		//var r = d.radius + radius.domain()[1] + padding,
		var r = maxRadius + maxRadius,
			nx1 = d.x - r,
			nx2 = d.x + r,
			ny1 = d.y - r,
			ny2 = d.y + r;
		quadtree.visit(function(quad, x1, y1, x2, y2) {
			if (quad.point && (quad.point !== d)) {
				var x = d.x - quad.point.x,
					y = d.y - quad.point.y,
					l = Math.sqrt(x * x + y * y),
					//r = d.radius + quad.point.radius + (d.color !== quad.point.color) * padding;
					r = maxRadius + maxRadius + (d.cluster === quad.point.cluster ? padding : clusterPadding);
				if (l < r) {
					l = (l - r) / l * alpha;
					d.x -= x *= l;
					d.y -= y *= l;
					quad.point.x += x;
					quad.point.y += y;
				}
			}
			return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
		});
	};
}

function linkOpacity(val) {
	if(val<0.3)
		return 0.2;
	else if(val<0.5)
		return 0.3;
	else if (val < 0.7)
		return 0.5;
	else if (val < 0.8)
		return 0.7;
	else
		return 0.8;
}

function fadeRelativeToNode(opacity) {
	return function(d) {		
		if(opacity < 1) {
			node.style("opacity", function(o) {
				var thisOpacity = isConnected(d, o) ? 1 : opacity;
				this.setAttribute('stroke-opacity', thisOpacity);
				if(thisOpacity == 1 && d!=o) {
					d3.select(this).select("text").transition()
						.duration(200)
						.style("visibility", "visible");
				}
				else {
					d3.select(this).select("text").transition()
						.duration(200)
						.style("visibility", "hidden");
				}
				return thisOpacity;
			});

			//link.style("stroke-opacity", function(o) {
			//	return o.source === d || o.target === d ? 1 : opacity;
			//});
			var htmltext;
			if(d.multicluster <= 1) // only belong one cluster
				htmltext = d.title;
			else { // belong to multiple clusters
				var grpary = String(d.group).split(","), index;
				htmltext = d.title + ", in groups ";
				for	(index = 0; index < grpary.length; index++) {
					var hexclr =  d3.rgb(color(+grpary[index]));
					htmltext += "<span class=\"numberCircle\" style=\"background:"+hexclr+"\">" + grpary[index].trim() + "</span>";					
				}
			}
			tooltip.html(htmltext)  
				.style("left", (d3.event.pageX) + "px")     
				.style("top", (d3.event.pageY - 28) + "px");

			tooltip.transition()        
				.duration(200)      
				.style("visibility", "visible");    
		}
		else { // return to no-fading
			node.style("opacity", function(o) {
				this.setAttribute('stroke-opacity', 0.9);
				d3.select(this).select("text").transition()
						.duration(200)
						.style("visibility", o.multicluster>1?"visible":"hidden");
			});
			
			//link.style("stroke-opacity", 0.6);
			tooltip.transition()        
				.duration(500)      
				.style("visibility", "hidden");
		}
	}
}  
function fadeRelativeToLink(opacity) {
	return function(d) {
		if (typeof(node) != "undefined")
			node.style("stroke-opacity", function(o) {
				var thisOpacity = (o==d.source || o==d.target ? 0.9 : opacity);
				this.setAttribute('opacity', thisOpacity);
				return thisOpacity;
			});

		link.style("stroke-opacity", function(o) {
			return o === d ? 1 : opacity;
		});
		if(opacity < 1 && d3.event != null) {
			tooltip.transition()        
				.duration(200)      
				.style("visibility", "visible");      
			tooltip.html("Similarity measure: "+d.value)
				.style("left", (d3.event.pageX) + "px")     
				.style("top", (d3.event.pageY - 28) + "px");
		}
		else { // return to no-fading
			link.style("stroke-opacity", function(o) {
				return linkOpacity(o.value);				
			});
			tooltip.transition()        
				.duration(500)      
				.style("visibility", "hidden");
		}
	}
}	
function FilterBySimVal(aryvalue, index, ar) {
	if(aryvalue.value <= simVal)
		return false;
	else
		return true;
}	

function SetupLink() {
	link = svg.selectAll("path.link")
		.data(filterData);
	
	link.enter().append("path")
		.attr("class", "link")
		.style("stroke-opacity", function(d) { return linkOpacity(d.value); })
		.style("stroke-width", 2)
		.on("mouseover", fadeRelativeToLink(0.3))
		.on("mouseout", fadeRelativeToLink(1))
		.on("click", function(d) {
			// clear out previously clicked/hgted other links if any
			if(lastSelLink != null)
				lastSelLink.style("stroke", "#999");
			selEdgeSource = d.source;
			selEdgeTarget = d.target;	
			if(lastSelEdgeSource != selEdgeSource || lastSelEdgeTarget != selEdgeTarget) {	
				lastSelLink = d3.select(this);
				lastSelLink.transition() 
					.duration(500)
					.style("stroke", "red");
				d3.select("#datainfo").html("Similarity measure between node " + d.source.title + " and node " + d.target.title + " is "+d.value);
				lastSelEdgeSource = selEdgeSource;
				lastSelEdgeTarget = selEdgeTarget; 	
			}
			else {// clear out the selection if the selected link is clicked again
				d3.select(this).transition() 
					.duration(500)
					.style("stroke", "#999");
				lastSelLink = null;	
				lastSelEdgeSource = -1;
				lastSelEdgeTarget = -1;
				d3.select("#datainfo").html(""); 	
			}
			// clear out previously clicked/hgted other nodes if any
			if(lastSelNode != null) {
				lastSelNode.style("stroke", node_stroke_clr);
				lastSelNode = null;
				lastSelNodeId = -1;
			}
		});
	link.exit().remove(); // very important, otherwise, old data will not be removed
	link.style("stroke-opacity", function(d) {
			return linkOpacity(d.value);
		});
	return link;
}

function SetupData() {
	svg.selectAll('g').remove();
	link = SetupLink();
	
	node = svg.selectAll(".node")
		.data(oriData.nodes);
	node.exit().remove();	
	
	var gnode = node.enter().append("g")
		.attr("id", function(d, i) {var result = "N" + i; return result;})
		.attr("data-legend",function(d) { return d.clr})
		.attr("data-legend-color",function(d) { return color(d.clr)})
		.on("click", function(d) {
			if(lastSelNodeId != d.index) {
				// clear out previously clicked/hgted other nodes if any
				if(lastSelNode != null) {
					lastSelNode.style("stroke", function(d) { 
									if(d.multicluster<=1)
										return node_stroke_clr;
									else
										return node_multicluster_stroke_clr;
									})
								.style("stroke-width", function(d) { 
									return d.multicluster; 
									});					
				}
				lastSelNode = d3.select(this).select("circle");
				lastSelNode.transition() 
					.duration(500)
					.style("stroke", d3.rgb(255, 0, 0));
				var htmltext;
				if(d.multicluster <= 1) // only belong to one cluster
					htmltext = "<h2>"+d.title+"</h2>"+d.description;
				else { // belong to multiple clusters
					var grpary = String(d.group).split(","), index;				
					htmltext = "<h2>"+d.title + ", in groups ";
					for	(index = 0; index < grpary.length; index++) {
						var hexclr =  d3.rgb(color(+grpary[index]));
						htmltext += "<span class=\"numberCircle\" style=\"background:"+hexclr+"\">" + grpary[index].trim() + "</span>";					
					}
					htmltext += "</h2>"+d.description;
				}
				d3.select("#datainfo").html( htmltext);
				lastSelNodeId = d.index; 	
			}
			else {// clear out the selection if the selected node is clicked again
				lastSelNode = null;
				lastSelNodeId = -1;
				d3.select(this).select("circle").transition() 
					.duration(500)
					.style("stroke", function(d) { 
						if(d.multicluster<=1)
							return node_stroke_clr;
						else
							return node_multicluster_stroke_clr;
						})
					.style("stroke-width", function(d) { 
						return d.multicluster; 
						});	
				d3.select("#datainfo").html(""); 	
			}
			// clear out previously clicked/hgted link if any
			if(lastSelLink != null) {
				lastSelLink.style("stroke", "#999");
				lastSelLink = null;
				lastSelEdgeSource = -1;
				lastSelEdgeTarget = -1;
			}
		})	
		.call(force.drag)
			.on("mouseover", fadeRelativeToNode(0.3))
			.on("mouseout", fadeRelativeToNode(1))
		.call(node_drag);	
	
	bDynamicLayout = true;
	document.getElementById("togglelayout").value = "Pause layout";
	
	force
		.nodes(node.data())
		.links(link.data())
		//.linkStrength(0) // remove link influence to force-layout so that layout is only controlled by node clusters
		//.linkStrength(function(d, i) {return (d.value - 0.5>0 ? 0.01 : 0)})
		.linkStrength(function(d, i) {return d.value<simVal ? 0 : Math.pow(d.value, 2)})
		.on("tick", tick)
		.on("end", function() {
			//document.getElementById("togglelayout").click();
			document.getElementById("togglelayout").value = "Resume layout";
			bDynamicLayout = false;
		})
		.on("start", function() {
			//document.getElementById("togglelayout").click();
			document.getElementById("togglelayout").value = "Pause layout";
			bDynamicLayout = true;
		})	
		.start();
	force.nodes().forEach(function(d) {
		if(!(d.clr in clusterCenterNodes) || (d.weight > clusterCenterNodes[d.clr].weight)) {
			clusterCenterNodes[d.clr] = d;
		} 		
	});
	
	gnode.append("circle")	
		.attr("class", "circle")   
		.attr("r", function(d) {
					w = force.links().filter(function(p){return p.source == d || p.target == d}).length;
					if(w < minRadius)
						w = minRadius;
					else if(w > maxRadius)
						w = maxRadius;
					return w; })
		//.style("fill", d3.rgb(142, 186, 229))
		.style("fill", function(d) { 
			return color(d.clr); 
			})
		.style("opacity", 0.9)
		.style("stroke", function(d) { 
			if(d.multicluster<=1)
				return node_stroke_clr;
			else
				return node_multicluster_stroke_clr;
			})
		.style("stroke-width", function(d) { 
			return d.multicluster; 
			});
		
	gnode.append("text")
		.attr("x", 12)
		.attr("dy", ".35em")
		.style("visibility", function(d) {
			return d.multicluster>1? "visible":"hidden";
		})
		.text(function(d) {
			return d.multicluster>1? "Multiple-"+d.title:d.title;
		});
	
	linkedByIndex = {};
	link.data().forEach(function(d) {
		linkedByIndex[d.source.index + "," + d.target.index] = 1;
	});	
	legend = svg.append("g")
		.attr("class","legend")
		.attr("transform","translate(50,30)")
		.style("font-size","12px")
		.call(d3.legend);
}
	
function checkDuplicateEdge(e1, e2) {
	if((e1.source==e2.source && e1.target==e2.target) || (e1.source==e2.target && e1.target==e2.source))
		return true;
	else
		return false;
}

function FileChange() {
	var get_id = document.getElementById('data-chooser');
	var selval = get_id.options[get_id.selectedIndex].value;
	var fname;
	if(selval=="")
		fname = "";
	else
		fname = "data/"+selval+".json";
	if(curFileName==fname) return;
	curFileName = fname;
	if(fname=="") 
		return;
	$("#data-desc").html("This network shows the data-to-data relationship in Harris surveys extracted from <a href=\"http://arc.irss.unc.edu/dvn/\">Odum Institute Dataverse Network</a> at UNC-Chapel Hill. A categorical data similarity measurement algorithm was used to extract a similarity adjancey matrix that was then used to create this data-to-data relationship graph. Each node represents a Harris survey data record; each edge links the pair of nodes based on their similarity measurement --- the darker the edge, the more similar the linked nodes.");
	d3.json(fname, function(error, data) {
		var i;
		
	    force.stop(); // stops the force auto positioning before changing data
		bDragOn = true;
		oriData = {};
		filterData = [];
		for(i = data.links.length-1; i >= 0; i--){
			for(var j=i-1; j>=0; j--) {
				if(checkDuplicateEdge(data.links[i],data.links[j])) {
					data.links.splice(i,1);
					break;
				}
			}	
		}
		
		data.nodes.forEach(function (node) {
				if(!node.group)
					node.group = 0;
				var grpStrArys = String(node.group).split(","); 
				node.clr = grpStrArys[0].trim();
				node.multicluster = grpStrArys.length;
		});
	
		oriData = data;
		filterData = data.links.filter(FilterBySimVal);
		SetupData();
        force.resume();
		bDragOn = false;
	});
}

// ** Update data section (Called from the onclick)
function SimValueChange() {
	var get_id = document.getElementById('sim-value');
	simVal = get_id.options[get_id.selectedIndex].value;
	filterData = oriData.links.filter(FilterBySimVal);
	link = SetupLink();
	
	node.call(force.drag)
		.call(node_drag);
	
	force.nodes(node.data())
		 .links(link.data())
		 .linkStrength(function(d, i) {return d.value<simVal ? 0 : Math.pow(d.value, 2)})
		 .start();	

	linkedByIndex = {};
	link.data().forEach(function(d) {
		linkedByIndex[d.source.index + "," + d.target.index] = 1;
	});		
}

function ResetView() {
	zoom.scale(1);
	zoom.translate([0, 0]);
	//svg.transition().duration(500).attr('transform', 'translate(' + zoom.translate() + ') scale(' + zoom.scale() + ')');
	svg.attr("transform", "translate(" + zoom.translate() + ")scale(" + zoom.scale() + ")");
}

function ToggleLayout(ref) {
	if(bDynamicLayout) {
		ref.value="Resume layout";
		bDynamicLayout = false;
		force.stop();
	}
	else {
		ref.value="Pause layout";
		bDynamicLayout = true;	
		force.resume();
	}
}

function RestoreNode(id) {
	var selectorstr = "g.node[id='"+id+"']"; 
	d3.select(selectorstr).select("text").transition()
		.duration(200)
		.style("visibility", "hidden");	
	d3.select(selectorstr).select("circle").transition()
		.duration(200)	
		.style("stroke", node_stroke_clr);	
}

function HgtNode(id) {
	var selectorstr = "g.node[id='"+id+"']";
	d3.select(selectorstr).select("text").transition()
		.duration(200)
		.style("visibility", "visible");	
	d3.select(selectorstr).select("circle").transition()
		.duration(200)	
		.style("stroke", d3.rgb(255, 0, 0));	
}

function updateSearch(querystr) {
	var k;
	if(querystr == "") {
		if(lastSearchNodes.length > 0) {
			for(k=0; k<lastSearchNodes.length; k++) 
				RestoreNode(lastSearchNodes[k]);
			lastSearchNodes.splice(0, lastSearchNodes.length);
		}
		lastSearchStr = "";
	}
	else if (querystr != lastSearchStr) {
		// clear out previously highlighted nodes
		if(lastSearchNodes.length > 0) {
			for(k=0; k<lastSearchNodes.length; k++) 
				RestoreNode(lastSearchNodes[k]);
			lastSearchNodes.splice(0, lastSearchNodes.length);
		}
		var partsOfStr = querystr.split(','); // comma is used for boolean or of different search terms
		var compoundstr = "";
		for(k=0; k<partsOfStr.length; k++) {
			partsOfStr[k].trim();
			if(partsOfStr[k]=="") continue;
			if(k==0) // last part
				compoundstr += partsOfStr[k];
			else
				compoundstr += "|" + partsOfStr[k];	
		}
		lastSearchStr = querystr;
		if(compoundstr == "") return;	
		var reqry = new RegExp(compoundstr, "i"); // do a case-insensitive and boolean or search match
	    oriData.nodes.forEach(function(d, i) {
			if((d.title).match(reqry)) {
				var idstr = "N" + i;
				lastSearchNodes.push(idstr);
				HgtNode(idstr);
			}
		});	       
  	}		
}
