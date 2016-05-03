#!/usr/bin/env python3
import sys
import os
import os.path
import subprocess
import tempfile
import shutil
import argparse
from glob import glob

_default_printer = "utax3560i"

_clean_dirs = []
_tmpdir = None
_empty_pdfs = dict()


def get_temp_dir():
	global _tmpdir
	if _tmpdir is None:
		_tmpdir = tempfile.mkdtemp()
		_clean_dirs.append(_tmpdir)
	
	return _tmpdir


def cleanup():
	for d in _clean_dirs:
		shutil.rmtree(d)


def get_student_from_dir(directory):
	return os.path.basename(directory).rsplit("_", 1)[0]


def get_page_count(filename):
	return int(list(filter(
		lambda x: x.startswith("Pages: "),
		subprocess.check_output([ "pdfinfo", filename ]).decode("utf-8", "replace").split("\n")
	))[0].split()[1])


def get_page_size(filename, page=-1):
	page_count = get_page_count(filename)
	
	if page < 0:
		page = page_count + page + 1
	
	lines = [ x.split() for x in subprocess.check_output([ "pdfinfo", filename, "-f", str(page), "-l", str(page) ]).decode("utf-8", "replace").split("\n") ]
	# Page    1 size: 842 x 595 pts (A4)
	# Page    1 rot:  0
	size = list(filter(lambda x: len(x) > 5 and x[0] == "Page" and x[1] == str(page) and x[2] == "size:", lines))[0]
	rot = list(filter(lambda x: len(x) > 3 and x[0] == "Page" and x[1] == str(page) and x[2] == "rot:", lines))[0]
	
	width = float(size[3])
	height = float(size[5])
	rot = int(rot[3])
	
	return (width, height, rot)


def is_portrait(filename, page=-1):
	(width, height, rot) = get_page_size(filename, page)
	
	portrait = (height >= width)
	
	if rot in [ 90, 270 ]:
		portrait = not portrait
	
	return portrait


def concat(infiles, outfile):
	subprocess.check_call([ "pdftk" ] + infiles + [ "cat", "output", outfile ])


#def create_empty_pdfs():
#	tex_portrait = r"\documentclass[a4paper]{article}\begin{document}\thispagestyle{empty}\quad\end{document}"
#	tex_landscape = r"\documentclass[a4paper]{article}\usepackage[landscape]{geometry}\begin{document}\thispagestyle{empty}\quad\end{document}"
#	
#	tmpdir = get_temp_dir()
#	
#	with open(os.path.join(tmpdir, "empty-portrait.tex"), "w") as f:
#		print(tex_portrait, file=f)
#	with open(os.path.join(tmpdir, "empty-landscape.tex"), "w") as f:
#		print(tex_landscape, file=f)
#	
#	subprocess.check_call([ "pdflatex", os.path.join(tmpdir, "empty-portrait.tex") ], cwd=tmpdir)
#	subprocess.check_call([ "pdflatex", os.path.join(tmpdir, "empty-landscape.tex") ], cwd=tmpdir)
#	
#	return (
#		os.path.join(tmpdir, "empty-portrait.pdf"),
#		os.path.join(tmpdir, "empty-landscape.pdf")
#	)

def get_empty_pdf(width, height, rot):
	if (width, height, rot) in _empty_pdfs:
		return _empty_pdfs[(width, height, rot)]
	
	filename = os.path.join(get_temp_dir(), "empty-%s-%s-%s.pdf" % (width, height, rot))
	
	if rot == 0:
		print("Creating empty PDF file: %sx%s, %s°" % (width, height, rot))
		subprocess.check_call([ "convert", "xc:none", "-page", "%sx%s" % (width, height), filename ])
	
	else:
		rotstr = None
		if rot == 90:
			rotstr = "east"
		elif rot == 180:
			rotstr = "south"
		elif rot == 270:
			rotstr = "west"
		else:
			raise Exception("Unsupported rotation: %s" % rot)
		
		base_pdf = get_empty_pdf(width, height, 0)
		print("Creating empty PDF file: %sx%s, %s°" % (width, height, rot))
		subprocess.check_call([ "pdftk", "A=%s" % base_pdf, "cat", "A%s" % rotstr, "output", filename ])
	
	_empty_pdfs[(width, height, rot)] = filename
	return filename


def get_empty_pdf_for_page(filename, page=-1):
	page_count = get_page_count(filename)
	
	if page < 0:
		page = page_count + page + 1
	
	(width, height, rot) = get_page_size(filename, page)
	
	return get_empty_pdf(width, height, rot)


def merge_by_user(dirs, outdir):
	# Expected directories:
	# 1_Aufgabe_1_1_Hello_World__2722
	# containing student directories like
	# Gropp_Martin_12345/
	
	student_pdfs = dict()
	
	for assignment_dir in dirs:
		for student_dir in glob(os.path.join(assignment_dir, "*")):
			if not os.path.isdir(student_dir):
				continue
			
			student = get_student_from_dir(student_dir)
			if not student in student_pdfs:
				student_pdfs[student] = []
			
			for pdf in glob(os.path.join(student_dir, "*.pdf")):
				if not os.path.isfile(pdf):
					continue
				
				student_pdfs[student].append(pdf)
				
				if get_page_count(pdf) % 2 != 0:
					empty = get_empty_pdf_for_page(pdf)
					student_pdfs[student].append(empty)
	
	
	merged_pdfs = []
	for student in student_pdfs:
		pdfs = student_pdfs[student]
		
		outfile = os.path.join(outdir, "%s.pdf" % student)
		print(outfile)
		merged_pdfs.append(outfile)
		
		concat(pdfs, outfile)
	
	return merged_pdfs


def print_pdfs(filenames, printer):
	for filename in filenames:
		portrait = is_portrait(filename, 1)
		print("Printing %s (%s) on %s" % (filename, "portrait" if portrait else "landscape", printer))
		if portrait:
			subprocess.check_output([
				"lpr", "-P%s" % printer,
				#"-o", "Duplex=DuplexNoTumble",
				"-o", "KCStaple=UpperLeft",
				filename
			])
		else:
			subprocess.check_output([
				"lpr", "-P%s" % printer,
				"-o", "Duplex=DuplexTumble",
				"-o", "KCStaple=UpperRight",
				"-o", "KCRotate=True",
				filename
			])


if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Merge student assignment PDF files and print them.')
	parser.add_argument('dirs', metavar='dir', nargs='+', help='input directories with assignment PDFs')
	parser.add_argument('--out', dest='out', action='store', default=None, help='output directory')
	parser.add_argument('--print', dest='print', action='store_true', default=False, help='print merged files')
	parser.add_argument('--printer', dest='printer', action='store', default=_default_printer, help='set printer')
	
	args = parser.parse_args()
	
	if args.out is None and not args.print:
		print("You have to specify at least one of --out, --print!", file=sys.stderr)
		sys.exit(1)
	
	if args.out is None:
		args.out = os.path.join(get_temp_dir(), "pdfs")
		os.mkdir(args.out)
	
	merged_pdfs = merge_by_user(args.dirs, args.out)
	merged_pdfs.sort()
	if args.print:
		print_pdfs(merged_pdfs, args.printer)
	
	cleanup()
