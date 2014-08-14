local component = require("component")
local fs = require("filesystem")

local gpu = {}

-------------------------------------------------------------------------------

function gpu.importFile(path)
	checkArg(1, path, "string")

	if not component.isAvailable("ocl_gpu") then
		error("no OCLights2 GPU found", 2)
	end

	path = fs.canonical("/" .. path)
	if fs.isLink(path) then
		_, path = fs.isLink(path)
	end
	local fsproxy, mntpath = fs.get(path)
	return component.ocl_gpu.import(fsproxy.address, path:sub(#mntpath + 1))
end

-------------------------------------------------------------------------------

setmetatable(gpu, {
__index = function(_, key)
	if not component.isAvailable("ocl_gpu") then
		error("no OCLights2 GPU found", 2)
	end
	return component.ocl_gpu[key]
end})

-------------------------------------------------------------------------------

return gpu
