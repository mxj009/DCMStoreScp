
#coding:utf-8
import sys, os
import PIL.Image
from PIL import Image

imgPath = sys.argv[1]




#调整宽高
def auto_resize(im,scan):
	size =im.size
	width=int(scan*size[0])
	height = int(scan*size[1])
	return (width, height)
#计算转换率
def auto_scan(im,maxlength):
	size=im.size
	if(size[0]>size[1]):
		scan=(int(maxlength))/size[0]
	else:
	    scan=(int(maxlength))/size[1]
	return scan

def my_test(imgPath):
	im = Image.open(imgPath)
	scan=auto_scan(im,'1024')
	size = auto_resize(im, scan)
	new_im = im.resize(size)
	new_im.convert("RGB")
	img1024Path= imgPath.replace('.jpg','_thumb.jpg');
	new_im.save(img1024Path, "JPEG")
	print('ok')

my_test(imgPath)