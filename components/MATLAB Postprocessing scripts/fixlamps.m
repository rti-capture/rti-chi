function fixlamps(filefilter, datafile)
    
    dir_struct = dir(filefilter);
    [sorted_names,sorted_index] = sortrows({dir_struct.name}');
    
    n = length(sorted_names);
    if (mod(n,90)~=0)
        disp('The number of lights is not a multiple of 90');
        return;
    end
    
    lampcorrection = load(datafile);
        
    for i = 1:n
        fn = char(sorted_names(i));    
        
        clear Iout Iin;
        Iin = imread(fn);

        l = mod(i-1,90)+1;
        Iout(:,:,1) = Iin(:,:,1)*lampcorrection(l,1);
        Iout(:,:,2) = Iin(:,:,2)*lampcorrection(l,2);
        Iout(:,:,3) = Iin(:,:,3)*lampcorrection(l,3);
        
        imwrite(Iout,fn);
        disp([num2str(l) ': ' fn ' parsed.']);        
    end
end
