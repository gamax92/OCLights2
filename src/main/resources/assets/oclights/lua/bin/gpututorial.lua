local component = require("component")
local event = require("event")

if not component.isAvailable("ocl_gpu") then
	error("Could not find a GPU connected to the computer!",0)
end

local gpu = component.ocl_gpu

local sectex

function fadeDouble(v,x,y,r,g,b)
	x, y = x or 0, y or 0
	r,g,b = r or 0,g or 0,b or 0
	gpu.push()
	gpu.scale(2,2)
	for i=1,16 do
		gpu.setColor(r,g,b,32)
		gpu.drawText(v,x,y)
		os.sleep(0)
	end
	gpu.setColor(r,g,b)
	gpu.drawText(v,x,y)
	gpu.pop()
end

function fade(v,x,y,r,g,b)
	x, y = x or 0, y or 0
	r,g,b = r or 0,g or 0,b or 0
	for i=1,4 do
		gpu.setColor(r,g,b,64)
		gpu.drawText(v,x,y)
		os.sleep(0)
	end
	gpu.setColor(r,g,b)
	gpu.drawText(v,x,y)
end

local ny = 0
function fadeNext(v,r,g,b)
	fade(v,0,ny,r,g,b)
	ny = ny+8
end
function fadeDoubleNext(v,r,g,b)
	fadeDouble(v,0,ny,r,g,b)
	ny = ny+16
end

function fadeScreen()
	--copy screen to sectex
	for i=0,255,16 do
		gpu.setColor(255,255,255,i)
		gpu.filledRectangle(0,0,gpu.getSize(0))
		os.sleep(0)
	end
	gpu.setColor(255,255,255)
	gpu.fill()
	ny = 0
end

local examples = {
	{"Colors and Fills",
	function()
		fade "Screen filling"
		fade("gpu.setColor(128,128,255)",0,8)
		fade("gpu.fill()",0,16)
		fade("Click to continue",0,24)
		event.pull("monitor_up")
		
		gpu.setColor(128,128,255)
		gpu.fill()
		
		fade "The screen is filled"
		fade("Click to continue",0,8)
		event.pull("monitor_up")
		fadeScreen()
	end
	},
	{"Rectangles",
	function()
		fade "Outlined Rectangles:"
		fade("gpu.rectangle(25,25,50,50)",0,8)
		gpu.setColor(128,128,255)
		gpu.rectangle(25,25,50,50)
		
		fade("Click to continue",0,16)
		event.pull("monitor_up")
		fadeScreen()
		
		fade "Filled Rectangles:"
		fade("gpu.filledRectangle(25,25,50,50)",0,8)
		gpu.setColor(128,128,255)
		gpu.filledRectangle(25,25,50,50)
		
		fade("Click to continue",0,16)
		event.pull("monitor_up")
		fadeScreen()
	end},
	{"Textures",
	function()
		fadeNext("In OCLights2, you can use")
		fadeNext("gpu.createTexture(width,height)")
		fadeNext("to create textures.")
		fadeNext("The above method will return a number")
		fadeNext("That number is the texture id")
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
		
		fadeNext("Also, textures come with a cost")
		fadeNext("The ingame GPU starts with 8kb of RAM")
		fadeNext("A texture will use (w*h)/32 bytes of RAM")
		fadeNext("There are methods to measure RAM usage")
		fadeNext("gpu.getUsedMemory()")
		fadeNext("gpu.getFreeMemory()")
		fadeNext("gpu.getTotalMemory()")
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
		
		fadeNext("Because OCLights2 has limited texture memory,")
		fadeNext("you can free textures.")
		fadeNext("gpu.freeTexture(id)")
		fadeNext("Given an id, the above will free that texture")
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
		
		fadeNext("In OCLights2, the screen texture has an id of 0")
		fadeNext("You can use the screen like a texture")
		fadeNext("gpu.drawTexture(0,64,64)")
		os.sleep(0.5)
		gpu.setColor(255,255,255)
		gpu.drawTexture(0,64,64)
		fadeNext("Now the screen is onscreen!")
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
		
		fadeNext("Textures in OCLights2 are framebuffers")
		fadeNext("This means that you can draw to them")
		fadeNext("gpu.bindTexture(id) allows you to do that")
		fadeNext("gpu.bindTexture(0) will draw to the screen")
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
		
		fadeNext("Example Code:")
		
		fadeNext("local tex = gpu.createTexture(32,32)")
		
		fadeNext("gpu.bindTexture(tex)")
		fadeNext("gpu.setColor(255,0,0)")
		fadeNext("gpu.filledRectangle(8,8,16,16)")
		fadeNext("gpu.bindTexture(0)")
		fadeNext("gpu.setColor(255,255,255)")
		fadeNext("gpu.drawTexture(tex,64,64)")
		fadeNext("gpu.drawTexture(tex,128,128)")
		fadeNext("gpu.freeTexture(tex)")
		
		local tex = gpu.createTexture(32,32)
		gpu.bindTexture(tex)
		gpu.setColor(255,0,0)
		gpu.filledRectangle(8,8,16,16)
		gpu.bindTexture(0)
		gpu.setColor(255,255,255)
		gpu.drawTexture(tex,64,64)
		gpu.drawTexture(tex,128,128)
		gpu.freeTexture(tex)
		
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
		
		fadeNext("That example did the following:")
		fadeNext("Created a 32 by 32 texture")
		fadeNext("Binded the new texture")
		fadeNext("Set the draw color to red")
		fadeNext("Rendered a rectangle on the texture")
		fadeNext("Binded the screen")
		fadeNext("Set the draw color to white")
		fadeNext("Draw the texture at 64,64")
		fadeNext("Draw the texture at 128,128")
		fadeNext("Free the memory allocated for the texture")
		fadeNext("Click to continue")
		event.pull("monitor_up")
		fadeScreen()
	end},
	{"Importing Images",
	function()
		local file = io.open("/usr/misc/smile.png","rb")
		local data = file:read("*a")
		file:close()
		local t = gpu.import(data)
		fade("Importing images:")
		fade("local texture = gpu.import(\"smile.png\")",0,8)
		fade("gpu.drawTexture(texture,64,64)",0,16)
		fade("gpu.freeTexture(texture)",0,24)
		gpu.setColor(255,255,255)
		gpu.drawTexture(t,64,64)
		gpu.freeTexture(t)
		fade("Click to continue",0,32)
		event.pull("monitor_up")
		fadeScreen()
	end}
}

function runExample(i)
	fadeDouble("Example "..i)
	fade(examples[i][1],0,16)
	fade("Click to continue",0,24)
	event.pull("monitor_up")
	fadeScreen()
	
	examples[i][2]()
end

function askQuit()
	gpu.bindTexture(sectex)
	gpu.setColor(255,255,255)
	gpu.drawTexture(0,0,0)
	gpu.blur(sectex)
	gpu.bindTexture(0)
	
	for i=0,255,16 do
		gpu.setColor(255,255,255,i)
		gpu.drawTexture(sectex,0,0)
		gpu.setColor(0,0,0,math.floor(i/4))
		gpu.filledRectangle(0,0,gpu.getSize(0))
		os.sleep(0)
	end
	
	fadeDouble("Bye!",0,0,255,255,255)
	fade("Click to continue",0,16)
	event.pull("monitor_up")
	
	for i=0,255,16 do
		gpu.setColor(0,0,0,i)
		gpu.filledRectangle(0,0,gpu.getSize(0))
		os.sleep(0)
	end
end

local s,e = pcall(function()
	local w,h = gpu.getSize(0)
	sectex = gpu.createTexture(w,h)
	local q = false
	
	--parallel.waitForAny(function()
		for i=0,255,16 do
			gpu.setColor(i,i,i)
			gpu.fill()
			os.sleep(0)
		end
		gpu.setColor(255,255,255)
		gpu.fill()

		fadeDouble("OCLights2 Tutorial",0,0)
		fade("Click to continue",0,16)
		local exm = "Click me for the examples menu"
		fade(exm,0,24)
		gpu.setColor(255,0,0)
		gpu.filledRectangle(w-4,h-4,4,4)
		
		local _,b,x,y = event.pull("monitor_up")
		
		if x>=0 and x<=gpu.getTextWidth(exm) and y>=24 and y<=32 then
			fadeScreen()
			--load example menu
			--so, we bind to sectex, draw the menu, then we fade in
			
			local ent = math.floor(h/9)
			local tw = gpu.getTextWidth("<")
			
			local function redraw(i)
				gpu.bindTexture(sectex)
				gpu.setColor(0,0,0)
				gpu.fill()
				
				gpu.setColor(64,64,64)
				gpu.filledRectangle(0,0,tw,h)
				gpu.filledRectangle(w-tw,0,tw,h)
				
				gpu.setColor(0,0,0)
				gpu.drawText("<",0,h/2-3)
				gpu.drawText(">",w-tw,h/2-3)
				
				gpu.setColor(128,128,128)
				gpu.filledRectangle(tw,0,w-tw-tw,h)
				
				local x = tw+1
				local y = 1
				for i=i, i+ent-1 do
					if examples[i] then
						gpu.setColor(0,0,0)
						gpu.drawText(" - "..examples[i][1],x,y)
					end
					gpu.setColor(192,192,192)
					gpu.line(x-1,y-1,x-1+w-tw-tw,y-1)
					gpu.line(x-1,y+7,x-1+w-tw-tw,y+7)
					y = y+9
				end
				gpu.bindTexture(0)
			end
			
			redraw(1)
			for i=0,255,16 do
				gpu.setColor(255,255,255)
				gpu.fill()
				gpu.setColor(255,255,255,i)
				gpu.drawTexture(sectex,0,0)
				os.sleep(0)
			end
			while true do
				gpu.drawTexture(sectex,0,0)
				local _,b,mx,my = event.pull("monitor_up")
				
				local x = tw+1
				local y = 1
				for i=1, 1+ent-1 do
					if examples[i] then
						if mx>=x-1 and mx<=x-1+w-tw-tw and my>=y-1 and my<=y+8 then
							fadeScreen()
							runExample(i)
							redraw(1)
							for i=0,255,16 do
								gpu.setColor(255,255,255)
								gpu.fill()
								gpu.setColor(255,255,255,i)
								gpu.drawTexture(sectex,0,0)
								os.sleep(0)
							end
						end
					end
					y = y+9
				end
			end
		else
			fadeScreen()
			for i=1, #examples do
				runExample(i)
			end
			
			fade("You are finished with the OCLights2 tutorial!")
			fade("Click to continue",0,8)
			event.pull("monitor_up")
			fadeScreen()
		end
	--[[ end,function()
		while true do
			local _,b,x,y = event.pull("monitor_up")
		
			if x>=w-4 and y>=h-4 then
				q = true
				return
			end
		end
	end) --]]
	if q then askQuit() end
end)

if not s then
	print(s)
	if sectex then gpu.freeTexture(sectex) end
	gpu.setColor(0,0,0)
	gpu.fill()
	os.sleep(0)
	gpu.setColor(255,255,255)
	gpu.push()
	gpu.scale(2,2)
	gpu.drawText("Well, that was bad...",0,0)
	gpu.pop()
	os.sleep(0)
	gpu.drawText(e,0,16)
	print(e)
else
	gpu.freeTexture(sectex)
end
