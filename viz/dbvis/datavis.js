var width = 960,
    height = 500,
    radius = 16;

var tooltip2 = d3.select("#chart").append("div")   
    .attr("class", "tooltip")               
    .style("visibility", "hidden");

var tooltip3 = d3.select("#chart").append("div")   
    .attr("class", "tooltip")               
    .style("visibility", "hidden");

var tooltip = d3.select("#chart").append("div")   
    .attr("class", "tooltip")               
    .style("visibility", "hidden");

var lastSelNode = null, lastSelLink = null;
d3.json("Xing-Similarity.json", function(json) {
    for(var i = 0; i < json.links.length; i++){
      if(json.links[i].value <= .20)
        json.links.splice(i--,1);
    }
	var svg = d3.select("#chart").append("svg")
    .attr("width", width)
    .attr("height", height);

	var link = svg.selectAll("line.link")
      .data(json.links)
    .enter().append("line")
      .attr("class", "link")
      .style("stroke-linecap", "round")
      .style("stroke-opacity", function(d) {d.opacity = (d.value - .2) * 1.3; return d.opacity })
      //.style("stroke-dasharray", function(d) {return d.value > 1 ? "1, 0" : (d.value * 6) + ", " + (3 - d.value * 2)})
      .style("stroke-width", function(d) { false; return d.value*d.value*14 + 1; })
			.on("mouseover", fadeRelativeToLink(0.1))
			.on("mouseout", fadeRelativeToLink(1))	
			.on("click", function(d) {
				// clear out previously clicked/hgted other nodes if any
				if(lastSelNode != null) {
					lastSelNode.style("fill", d3.rgb(142, 186, 229));
					lastSelNode = null;
				}
				// clear out previously clicked/hgted other links if any
				if(lastSelLink != null)
					lastSelLink.style("stroke", "#999");
				d3.select("#datainfo").html("Similarity measure between node " + d.source.name + " and node " + d.target.name + " is "+d.value); 
				lastSelLink = d3.select(this);
				lastSelLink.transition() 
					.duration(500)
					.style("stroke", "red");
			})
			.on("dblclick", function(d) {
				d3.select("#datainfo").html(""); 
				if(lastSelNode != null) {
					lastSelNode.style("fill", d3.rgb(142, 186, 229));
					lastSelNode = null;
				}
				lastSelLink = null;
				d3.select(this).transition() 
					.duration(500)
					.style("stroke", "#999");
			});
	var force = d3.layout.force()
		.nodes(json.nodes)
		.links(json.links)
		.gravity(.01)
		.charge(-1000)
		//.linkDistance(function(d, i) {return 500 - d.value * 400})
		.linkDistance(100)
		.linkStrength(function(d, i) {return Math.pow(d.value + .2, 2) - .2})
		//.gravity(.01)
		//.linkDistance(50)
    .size([width, height])
	.start();

	var node_drag = d3.behavior.drag()
        .on("dragstart", dragstart)
        .on("drag", dragmove)
        .on("dragend", dragend);

    function dragstart(d, i) {
	d.wasfixed = d.isfixed;
	if(d.wasfixed == null) d.wasfixed = false;
	d.fixed = true;
	d.isfixed = true;
	d.dragged = 0;
    }

    function dragmove(d, i) {
        d.px += d3.event.dx;
        d.py += d3.event.dy;
        d.x += d3.event.dx;
        d.y += d3.event.dy; 
	d.dragged++;
        //tick(); // this is the key to make it work together with updating both px,py,x,y on d !
    }

    function dragend(d, i) {
	if(d.dragged < 2){
	  d.fixed = !d.wasfixed;
	  d.isfixed = !d.wasfixed;
	}
        //d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
        //tick();
	d3.select(this).select("circle").style("stroke", function(d) {if(d.isfixed) return "red"; else return d3.rgb(142, 186, 229).darker()});
        force.resume();
    }
	
	var node = svg.selectAll(".node")
		.data(force.nodes())
		.enter().append("g")
		.attr("class", "node")     
			.on("click", function(d) {
						// clear out previously clicked/hgted link if any
						if(lastSelLink != null) {
							lastSelLink.style("stroke", "#999");
							lastSelLink = null;
						}
						// clear out previously clicked/hgted other nodes if any
						if(lastSelNode != null)
							lastSelNode.style("fill", d3.rgb(142, 186, 229));
						d3.select("#datainfo").html(d.dbID+": " + d.name); 
						lastSelNode = d3.select(this).select("circle");
						lastSelNode.transition() 
							.duration(500)
							.style("fill", "red");})
			.on("dblclick", function(d) {
					d3.select("#datainfo").html(""); 
					if(lastSelLink != null) {
						lastSelLink.style("stroke", "#999");
						lastSelLink = null;
					}
					lastSelNode = null;
					d3.select(this).select("circle").transition() 
						.duration(500)
						.style("fill", d3.rgb(142, 186, 229));})
			.call(force.drag)
				.on("mouseover", fadeRelativeToNode(0.1))
				.on("mouseout", fadeRelativeToNode(1))
			.call(node_drag);	
	
	node.append("circle")	
		.attr("r", radius - .75)
    .style("fill", d3.rgb(142, 186, 229))
		.style("opacity", 0.8)
		.style("stroke", function(d) {if(d.isfixed) return "red"; else return d3.rgb(142, 186, 229).darker()});
	node.append("text")
    .attr("x", 12)
    .attr("dy", ".35em")
    .text(function(d) { return ""; });
	
	var linkedByIndex = {};
  json.links.forEach(function(d) {
      linkedByIndex[d.source.index + "," + d.target.index] = 1;
  });

  function isConnected(a, b) {
      return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index;
  }

	force.on("tick", tick);
	function tick() {
		node
        .attr("transform", function(d) { 
					d.x = Math.max(radius, Math.min(width - radius, d.x));
					d.y = Math.max(radius, Math.min(height - radius, d.y));
  	    	return "translate(" + d.x + "," + d.y + ")"; });

		link.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; });
	};

	function fadeRelativeToNode(opacity) {
      return function(d) {
          node.style("stroke-opacity", function(o) {
              thisOpacity = isConnected(d, o) ? 1 : opacity + .2;
              this.setAttribute('fill-opacity', thisOpacity);
              return thisOpacity;
          });

          link.style("stroke-opacity", opacity).style("stroke-opacity", function(o) {
	      if(opacity < 1)
                return o.source === d || o.target === d ? o.opacity * 3 : o.opacity / 4;
	      else return o.opacity;
          });
					if(opacity < 1) {
						tooltip.transition()        
							.duration(200)      
	        		.style("visibility", "visible");      
					  tooltip.html(d.dbID+": "+d.name)  
					    .style("left", (d3.event.pageX) + "px")     
					    .style("top", (d3.event.pageY - 28) + "px");
					}
					else {
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
		            thisOpacity = (o==d.source || o==d.target ? 1 : opacity + .2);
		            this.setAttribute('fill-opacity', thisOpacity);
		            return thisOpacity;
		        });

          link.style("stroke-opacity", opacity).style("stroke-opacity", function(o) {
	      if(opacity < 1)
                return o === d ? o.opacity * 5 : o.opacity / 4;
	      else return o.opacity;
          });
					if(opacity < 1 && d3.event != null) {
						tooltip.transition()        
							.duration(200)      
	        		.style("visibility", "visible");      
					  tooltip.html("Similarity measure: "+d.value)   
					    .style("left", (d3.event.pageX - 85) + "px")     
					    .style("top", (d3.event.pageY - 28) + "px");
					   var lnode = d.target;
					   var rnode = d.source;
					   if(d.source.x < d.target.x){
					     var lnode = d.source;
					     var rnode = d.target;
					   }
					   tooltip2.transition().duration(200)
					      .style("left", (lnode.x + 5 - 180) + "px")
					      .style("top", (lnode.y + 90) + "px")
					      .style("fill", "white")
					      .style("stroke", "white")
					      .style("visibility", "visible");
					   tooltip2.html(lnode.name);
					   tooltip3.transition().duration(200)
					      .style("left", (rnode.x + 5 + 20) + "px")
					      .style("top", (rnode.y + 90) + "px")
					      .style("fill", "black")
					      .style("stroke", "black")
					      .style("visibility", "visible");
					   tooltip3.html(rnode.name);
					}
					else {
						//d3.selectAll("line").style("opacity", function(o){return o.value});
						d3.selectAll(".tooltip").transition()
							.duration(500)      
	        		.style("visibility", "hidden");
					}
      }
  } 
});
