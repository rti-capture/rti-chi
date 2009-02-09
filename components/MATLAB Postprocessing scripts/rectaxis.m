function rectaxis(filefilter, axisfile)
    
    dir_struct = dir(filefilter);
    [sorted_names,sorted_index] = sortrows({dir_struct.name}');
    
    n = length(sorted_names);
    
    axis = load(axisfile);
    projaxis = [axis(1) axis(2) 0];
    yaxis = [0 1 0];
    
    theta = acos(dot(projaxis, yaxis)/norm(projaxis));
    thetadeg = 180*(theta)/pi;
    for i = 1:n
        fn = char(sorted_names(i));    
        
        clear Iout Iin;
        Iin = imread(fn);
        
        Iout = imrotate(Iin,thetadeg,'bilinear','crop');
        
        imwrite(Iout,fn);
        disp([num2str(i) ': ' fn ' parsed.']);        
    end
end
